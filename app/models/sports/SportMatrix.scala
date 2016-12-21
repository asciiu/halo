package models.sports

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
  // example match name: Spurs +4 vs Suns -4
  private val matchMatrix = mutable.Map[String, OddsMatrixAB]()
  private val allBookNames = mutable.ListBuffer[String]()

  def keys = matchMatrix.keys.toList.sorted
  def bookNames = allBookNames.sorted.toSet

  def allOdds(key: String): List[SportsBookOdds] = {
    matchMatrix.get(key) match {
      case Some(matrix) => matrix.allOdds
      case None => List.empty[SportsBookOdds]
    }
  }

  def highA(key: String): Option[(String, Double)] = {
    matchMatrix.get(key) match {
      case Some(matrix) => matrix.highestA
      case None => None
    }
  }

  def highB(key: String): Option[(String, Double)] = {
    matchMatrix.get(key) match {
      case Some(matrix) => matrix.highestB
      case None => None
    }
  }

  def acceptData(events: Seq[SportsEvent]): Unit = {
    for (event <- events) {
      val eventName = event.name
      val eventTime = event.time
      val lines = event.lines

      println(s"$eventName $eventTime")

      for (line <- lines) {
        val lineName = line.name
        val odds = line.odds
        println(s"$lineName $odds")
      }
    }
  }

  def updateData(data: SportsBookData) = {
    allBookNames += data.bookname

    for (evt <- data.events) {
      val optionPairs = processPairs(evt)
      val eventName = evt.name

      for (pair <- optionPairs) {
        val key = s"${evt.name}: ${pair.optionA.name} vs ${pair.optionB.name}"
        val odds = SportsBookOdds(data.bookname, pair.optionA, pair.optionB)

        matchMatrix.get(key) match {
          case Some(om) =>
            om.upsertOdds(odds)
          case None =>
            val newMatrix = new OddsMatrixAB(pair.optionA.name, pair.optionB.name)
            newMatrix.upsertOdds(odds)
            matchMatrix += key -> newMatrix
        }
      }
    }
  }

  private def processPairs(sportevent: SportsEvent): List[SportsEventPair] = {
    val opts = sportevent.lines
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
          lepair.append(SportsEventPair(option, oppositeOpt))
          // remove the opposite so we don't process it again
          // on next loop
          allOptions.remove(allOptions.indexOf(oppositeOpt))
        case None => // no nothing
      }
    }
    lepair.toList
  }

  def addMatchOdds(bookName: String, matchName: String, line1: SportsEventLine, line2: SportsEventLine): Unit = {
    allBookNames += bookName

    if (matchMatrix.contains(matchName)) {
      val odds = SportsBookOdds(bookName, line1, line2)
      matchMatrix(matchName).upsertOdds(odds)
    } else {
      val part1 = line1.name
      val part2 = line2.name
      val oddsMatrix = new OddsMatrixAB(part1, part2)
      val odds = SportsBookOdds(bookName, line1, line2)
      oddsMatrix.upsertOdds(odds)
      matchMatrix += matchName -> oddsMatrix
    }
  }
}
