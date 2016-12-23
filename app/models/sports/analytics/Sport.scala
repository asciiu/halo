package models.sports.analytics

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import models.sports.{SportsData, SportsEvent}

import scala.collection.mutable

class Sport(val sportName: String) {

  // maps an event name to an actor that tracks the odds
  // TODO remove events that have expired
  private val eventOptions = mutable.Map[String, SportEventOptions]()

  /**
    * Update events for this sport.
    * @param events
    * @return collection of events that were updated
    */
  def update(events: Seq[SportsEvent]): Seq[SportsEvent] = {
    val updatedEvents = mutable.ListBuffer[SportsEvent]()

    for (event <- events) {
      // example: Saturday, Dec 24, 2016 11:00 AM
      val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a")
      //val name = event.name.replace("vs", ":")
      val name = event.name
      val time = event.time
      val localDateTime = LocalDateTime.parse(time, formatter)
      val key = s"$name:$localDateTime".replace(" ", "")

      val options = eventOptions.get(key) match {
        case Some(bettingOptions) => bettingOptions
        case None =>
          val newOptions = new SportEventOptions(name, localDateTime)
          eventOptions += key -> newOptions
          newOptions
      }

      // get a list of options that were updated
      val updated = options.update(event.lines)
      if (updated.length > 0) {
        updatedEvents += event.copy(lines = updated)
      }
    }

    updatedEvents
  }

  def eventNames = eventOptions.keys.toList

  def sportData: SportsData = {
    val events = eventOptions.values.map { options =>
        options.sportsEvent
    }.toSeq.sortBy(_.time)
    SportsData(sportName, events)
  }
}
