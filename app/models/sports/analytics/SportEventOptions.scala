package models.sports.analytics

import java.time.LocalDateTime

import models.sports.{SportsEvent, SportsEventLine, SportsEventPair}

import scala.collection.mutable

/**
  * Keeps tabs on all betting options with their respective
  * odds over time.
  * @param eventName e.g. "Lions +5"
  * @param time the time of the event
  */
class SportEventOptions(val eventName: String, val time: LocalDateTime) {

  // event name must be separated by hyphen
  require(eventName.contains(" - "))

  val options = mutable.Map[String, OddsTracker]()
  val parts = eventName.split(" - ")

  def receive(lines: Seq[SportsEventLine]) = {
    for (line <- lines) {
      val optionName = line.name

      if (options.contains(optionName)) {
        val nt = options(optionName)
        nt.trackMovement(line.odds)
      } else {
        val nt = new OddsTracker(optionName, line.odds)
        options += optionName -> nt
      }
    }
  }

  def getCurrentOdds(optionName: String): Double = {
    options.get(optionName) match {
      case Some(tracker) => tracker.currentOdds
      case None => 0.0
    }
  }

  def isExpired: Boolean = {
    val now = LocalDateTime.now()
    if (now.isAfter(time)) true
    else false
  }

  def pairs(): List[SportsEventPair] = {
    val allOptions = mutable.ListBuffer(options.keys.toList.sorted:_*)
    val lepair = mutable.ListBuffer[SportsEventPair]()
    val optionAs = mutable.ListBuffer[String]()
    val optionBs = mutable.ListBuffer[String]()

    for (option <- allOptions) {
      var opposite = option
      if (option.contains("Over")) {
        opposite = opposite.replace("Over", "Under")
      } else if (opposite.contains(parts(0))) {
        opposite = opposite.replace(parts(0), parts(1))

        if (option.contains("-")) {
          opposite = opposite.replace("-", "+")
        } else if (option.contains("+")) {
          opposite = opposite.replace("+", "-")
        }
      }

      val nameA = option
      val nameB = opposite
      val oddsA = options(nameA).currentOdds
      val oddsB = options(nameB).currentOdds
      val optionA = SportsEventLine(nameA, oddsA)
      val optionB = SportsEventLine(nameB, oddsB)
      lepair.append(SportsEventPair(optionA, optionB))
      val i = allOptions.indexOf(opposite)
      allOptions.remove(i)
    }
    lepair.toList
  }

  def sportsEvent: SportsEvent = {
    val lines: Seq[SportsEventLine] = options.map { case (optionName, tracker) =>
      SportsEventLine(optionName, tracker.currentOdds)
    }.toSeq.sortBy(_.name)
    SportsEvent(eventName, time.toString, lines)
  }
}

