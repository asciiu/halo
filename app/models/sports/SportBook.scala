package models.sports

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import collection.mutable

object SportBook {
  def props(sportName: String) = Props(new SportBook(sportName))
}

class SportBook(val sportName: String) extends Actor with ActorLogging {

  // maps an event name to an actor that tracks the odds
  private val eventRefs = mutable.Map[String, ActorRef]()

  override def preStart() = {
    log.info(s"Started ${self.path}")
  }

  override def postStop() = {
    log.info(s"Stopped ${self.path}")
  }

  def receive: Receive = {
    case events: Seq[SportsEvent] =>
      for (event <- events) {
        val name = event.name.replace("vs", "-")
        val time = event.time
        val lines = event.options
        val key = s"$name-$time".replace(" ", "")

        val ref = eventRefs.get (key) match {
          case Some(actor) => actor
          case None =>
            val newRef = context.actorOf(EventWatcher.props(name, time), name = key)
            eventRefs += key -> newRef
            newRef
        }

        ref ! lines
      }
  }

  def eventNames = eventRefs.keys.toList
}
