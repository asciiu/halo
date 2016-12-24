package models.sports

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.collection.mutable

object Normalizer {

  def process(data: SportsBookData): SportsBookData = {
    val pass1 = correctSportName(data)
    val pass2 = correctEventName(pass1)
    val pass3 = correctEventTime(pass2)

    pass3
  }

  def correctSportName(data: SportsBookData): SportsBookData = {
    // we shall normalize the sport names from the books because these sport names
    // may be labeled differently on various bookmakers sites
    val sportname = data.sport
    val normalized = sportname match {
      // Cloudbet
      case "American FootballUSANFL" => "NFL Football"
      case "BasketballUSANBA" => "NBA Basketball"

      // SportsBet
      case "Football - USA: NFL" => "NFL Football"
      case "Basketball - USA: NBA" => "NBA Basketball"

      case _ => sportname
    }

    data.copy(sport = normalized)
  }

  def correctEventName(data: SportsBookData): SportsBookData = {

    val newEvents = mutable.ListBuffer[SportsEvent]()
    for (event <- data.events) {

      // all event names should be in proper case
      // uppercase first letter lower case all else
      val name = event.name
        .toLowerCase()
        .split(' ')
        .map(_.capitalize)
        .mkString(" ")
        .replace(" Vs ", " vs ")

      val options = mutable.ListBuffer[SportsEventLine]()
      for (option <- event.lines) {
        val optionName = option.name
          .toLowerCase()
          .split(' ')
          .map(_.capitalize)
          .mkString(" ")
          .replace(" Ml", " ML")
        options.append(option.copy(name = optionName))
      }

      newEvents.append(event.copy(name = name, lines = options))
    }

    data.copy(events = newEvents)
  }

  /**
    * Normalize the event time so if fits nicely on a 15 minute
    * boundary. This is required because some bookmakers report
    * event time as 5:05pm instead of 5:00pm.
    * @param data
    * @return
    */
  def correctEventTime(data: SportsBookData): SportsBookData = {
    val newEvents = mutable.ListBuffer[SportsEvent]()
    for (event <- data.events) {

      // all event names should be in proper case
      // uppercase first letter lower case all else
      val time = event.time
      val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a")
      val newTime = LocalDateTime.parse(time, formatter)
      val unroundedMinutes = newTime.getMinute()
      val mod = unroundedMinutes % 30
      val delta = if (mod < 16) -mod else (30-mod)
      val rounded = newTime.plusMinutes(delta)

      newEvents.append(event.copy(time = rounded.format(formatter)))
    }

    data.copy(events = newEvents)
  }
}
