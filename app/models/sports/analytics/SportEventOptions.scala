package models.sports.analytics

import models.sports.{SportsEvent, SportsEventLine}

import scala.collection.mutable

/**
  * Keeps tabs on all betting options with their respective
  * odds over time.
  * @param eventName e.g. "Lions +5"
  * @param time the time of the event
  */
class SportEventOptions(val eventName: String, val time: String) {

  // TODO kill actor after time
  val options = mutable.Map[String, OddsTracker]()

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

  def sportsEvent: SportsEvent = {
    val lines: Seq[SportsEventLine] = options.map { case (optionName, tracker) =>
      SportsEventLine(optionName, tracker.currentOdds)
    }.toSeq
    SportsEvent(eventName, time, lines)
  }
}

