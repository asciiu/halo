package models.sports.analytics

import models.sports.{SportsData, SportsEvent}

import scala.collection.mutable

class Sport(val sportName: String) {

  // maps an event name to an actor that tracks the odds
  // TODO remove events that have expired
  private val eventRefs = mutable.Map[String, SportEventOptions]()

  /**
    * Receive events for this type of sport.
    * @param events
    */
  def receive(events: Seq[SportsEvent]) = {
      for (event <- events) {
        val name = event.name.replace("vs", "-")
        val time = event.time
        val lines = event.lines
        val key = s"$name-$time".replace(" ", "")

        val options = eventRefs.get (key) match {
          case Some(bettingOptions) => bettingOptions
          case None =>
            val newOptions = new SportEventOptions(name, time)
            eventRefs += key -> newOptions
            newOptions
        }

        options.receive(lines)
      }
  }

  def eventNames = eventRefs.keys.toList

  def sportData: SportsData = {
    val events = eventRefs.values.map { options =>
        options.sportsEvent
    }.toSeq
    SportsData(sportName, events)
  }
}
