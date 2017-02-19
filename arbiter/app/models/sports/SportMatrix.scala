package models.sports

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import common.models.halo.{BookOdds, EventData}

import scala.collection.mutable


object SportMatrix {
  def props(sportName: String) = Props(new SportMatrix(sportName))

  case class SendAllEvents(out: ActorRef)
  case class SendEventOdds(eventID: String, out: ActorRef)
  case class SendShiftedEvents(out: ActorRef, count: Option[Int])
}

/**
  * Keeps track of odds matrix for events that fall under
  * a particular sport - e.g. all event odds for NBA
  * basketball
  *
  * @param sportName
  */
class SportMatrix(val sportName: String) extends Actor with ActorLogging {
  import SportMatrix._

  // maps a match name to the odds matrix
  // example match name: Denver Nuggets vs Los Angeles Clippers: Denver Nuggets +7 vs Los Angeles Clippers -7
  private val matchMatrix = mutable.Map[String, OddsMatrixAB]()
  private val allBookNames = mutable.Set[String]()
  private val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a")

  // supports lookups by eventID
  private val sportingEventIds = mutable.Map[Int, String]()
  private val prefix = sportName.substring(0, 3)

  def keys = matchMatrix.keys.toList.sorted
  def bookNames = allBookNames.toList.sorted

  def allOdds(key: String): List[SportsBookOdds] = {
    matchMatrix.get(key) match {
      case Some(matrix) => matrix.allOdds
      case None => List.empty[SportsBookOdds]
    }
  }

  def highA(key: String): List[(String, Double)] = {
    matchMatrix.get(key) match {
      case Some(matrix) => matrix.highestA
      case None => List[(String, Double)]()
    }
  }

  def highB(key: String): List[(String, Double)] = {
    matchMatrix.get(key) match {
      case Some(matrix) => matrix.highestB
      case None => List[(String, Double)]()
    }
  }

  override def receive = {
    case data: SportsBookData =>
      updateData(data)

    case SendAllEvents(out) =>
      out ! gleam("")

    case SendShiftedEvents(out, countOpt) =>
      out ! shiftedEvents(countOpt)

    /**
      * Sends EventData object to sender
      */
    case SendEventOdds(eventID, out) =>
      val id = eventID.substring(3).toInt
      sportingEventIds.get(id) match {
        case Some(key) =>
          val oddsm = matchMatrix(key)
          out ! oddsm.allOddsHistory
        case None =>
          out ! EventData("NA", "NA", List[BookOdds]())
      }
  }

  /**
    * TODO
    * @return
    */
  def gleam(byDate: String): List[(String, OddsMatrixAB)] = {
    purgeExpiredData()

    matchMatrix.toList
  }

  def shiftedEvents(count: Option[Int]): List[(String, OddsMatrixAB)] = {
    purgeExpiredData()

    matchMatrix.filter(_._2.hasShifted(count.getOrElse(0))).toList
  }

  def purgeExpiredData() = {
    val pattern = """(\(\w+.*\))""".r

    // remove all expired events
    matchMatrix.foreach{ case (key, matrix) =>
      if (matrix.isExpired) matchMatrix.remove(key)
    }
  }

  def updateData(data: SportsBookData, debug: Boolean = true) = {
    // TODO there's a better way this breaks your tests
    //purgeExpiredData()

    allBookNames += data.bookname

    for (evt <- data.events) {
      val optionPairs = processPairs(evt)
      val eventName = evt.name
      // ordered alphabetically so we can guarantee uniques in map keys
      val normalizeName = evt.name.split(" vs ").sorted.mkString(" vs ")

      for (pair <- optionPairs) {
        val date = evt.time.split(" ").take(4).mkString(" ")
        val key = s"${normalizeName} ($date) : ${pair.optionA.name} vs ${pair.optionB.name}"
        val odds = SportsBookOdds(data.bookname, pair.optionA, pair.optionB)

        val matrix = matchMatrix.get(key) match {
          case Some(om) =>
            om.upsertOdds(odds)
            om
          case None =>
            // increment the event ID
            val newId = sportingEventIds.keys.toList.sorted.lastOption.getOrElse(0) + 1
            // track this id with the key name
            sportingEventIds += newId -> key

            val newMatrix = new OddsMatrixAB(prefix.concat(newId.toString), LocalDateTime.parse(evt.time, formatter), pair.optionA.name, pair.optionB.name)
            newMatrix.upsertOdds(odds)
            matchMatrix += key -> newMatrix
            newMatrix
        }

        // TODO remove
        if (debug) matrix.displayArb(key)
      }
    }
  }

  private def processPairs(sportevent: SportsEvent): List[SportsEventPair] = {
    val opts = sportevent.options
    val parts = sportevent.name.split(" vs ")

    // TODO need to use time in the keys
    val time  = sportevent.time
    val allOptions = mutable.ListBuffer(opts:_*)
    val lepair = mutable.ListBuffer[SportsEventPair]()

    for (option <- allOptions) {
      val name = option.name

      val oppositeOptionName =
        if (name.contains("Over")) {
          name.replace("Over", "Under")
        } else if (name.contains("Under")) {
          name.replace("Under", "Over")
        } else if (name.contains(parts(0))) {
          name.replace(parts(0), parts(1))
        } else if (name.contains(parts(1))) {
          name.replace(parts(1), parts(0))
        } else {
          name
        }

      val fullOpposite =
        if (oppositeOptionName.contains("-")) {
          oppositeOptionName.replace("-", "+")
        } else if (oppositeOptionName.contains("+")) {
          oppositeOptionName.replace("+", "-")
        } else {
          oppositeOptionName
        }

      allOptions.find(_.name == fullOpposite) match {
        case Some(oppositeOpt) =>
          val (optionA, optionB) = if (option.name < oppositeOpt.name) (option, oppositeOpt) else (oppositeOpt, option)

          lepair.append(SportsEventPair(optionA, optionB))
          // remove the opposite so we don't process it again
          // on next loop
          allOptions.remove(allOptions.indexOf(oppositeOpt))
        case None => // no nothing
      }
    }
    lepair.toList
  }
}
