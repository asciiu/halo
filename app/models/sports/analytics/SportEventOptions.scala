package models.sports.analytics

import java.time.LocalDateTime
import models.sports.{SportsEvent, SportsEventOption, SportsEventPair}
import scala.collection.mutable

/**
  * Keeps tabs on all betting options with their respective
  * odds over time.
  * @param eventName e.g. "Lions +5"
  * @param time the time of the event
  */
class SportEventOptions(val eventName: String, val time: LocalDateTime) {

  // event name must be separated by "vs"
  require(eventName.contains(" vs "))

  // option name mapped to odds over time
  // e.g. "Golden State Warriors +8" -> ...
  val options = mutable.Map[String, OddsTracker]()
  val parts = eventName.split(" vs ")

  /**
    * update the odds for each option
    * @param opts
    * @return a list of event lines options that were updated
    */
  def update(opts: Seq[SportsEventOption]): List[SportsEventOption] = {
    val updated = mutable.ListBuffer[SportsEventOption]()

    for (o <- opts) {
      // example: "Detriot Lions +3"
      val optionName = o.name

      if (options.contains(optionName)) {

        val changed = options(optionName).trackMovement(o.odds)
        if (changed) updated.append(o)

      } else {
        options += optionName -> new OddsTracker(optionName, o.odds)
        updated.append(o)
      }
    }
    updated.toList
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

  // TODO if this was a trait or an actor you could publish these things
  // accross the event bus and process and tract them in another actor
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
      val optionA = SportsEventOption(nameA, oddsA)
      val optionB = SportsEventOption(nameB, oddsB)
      lepair.append(SportsEventPair(optionA, optionB))
      val i = allOptions.indexOf(opposite)
      allOptions.remove(i)
    }
    lepair.toList
  }

  def sportsEvent: SportsEvent = {
    val lines: Seq[SportsEventOption] = options.map { case (optionName, tracker) =>
      SportsEventOption(optionName, tracker.currentOdds)
    }.toSeq.sortBy(_.name)
    SportsEvent(eventName, time.toString, lines)
  }
}

