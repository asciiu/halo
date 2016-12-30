package models.sports

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.collection.mutable

/**
  * Keeps track of odds matrix for events that fall under
  * a particular sport - e.g. all event odds for NBA
  * basketball
  *
  * @param sportName
  */
class SportMatrix(val sportName: String) {

  // maps a match name to the odds matrix
  // example match name: Denver Nuggets vs Los Angeles Clippers: Denver Nuggets +7 vs Los Angeles Clippers -7
  private val matchMatrix = mutable.Map[String, OddsMatrixAB]()
  private val allBookNames = mutable.Set[String]()
  private val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a")


  def keys = matchMatrix.keys.toSeq.sorted
  def bookNames = allBookNames.toSeq.sorted

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

  /**
    * TODO
    * @return
    */
  def gleam(byDate: String): List[(String, OddsMatrixAB)] = {
    purgeExpiredData()

    matchMatrix.toList
  }

  def purgeExpiredData() = {
    val pattern = """(\(\w+.*\))""".r

    // remove all expired events
    matchMatrix.foreach{ case (key, matrix) =>
      if (matrix.isExpired) matchMatrix.remove(key)
    }
  }

  def updateData(data: SportsBookData, debug: Boolean = true) = {
    purgeExpiredData()

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
            val newMatrix = new OddsMatrixAB(LocalDateTime.parse(evt.time, formatter), pair.optionA.name, pair.optionB.name)
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

//  def addMatchOdds(bookName: String, key: String, line1: SportsEventLine, line2: SportsEventLine): Unit = {
//    allBookNames += bookName
//
//    if (matchMatrix.contains(key)) {
//      val odds = SportsBookOdds(bookName, line1, line2)
//      matchMatrix(key).upsertOdds(odds)
//    } else {
//      val part1 = line1.name
//      val part2 = line2.name
//      val oddsMatrix = new OddsMatrixAB(part1, part2)
//      val odds = SportsBookOdds(bookName, line1, line2)
//      oddsMatrix.upsertOdds(odds)
//      matchMatrix += key -> oddsMatrix
//    }
//  }
}
