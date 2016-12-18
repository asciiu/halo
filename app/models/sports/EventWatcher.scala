package models.sports

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}

import scala.collection.mutable

object EventWatcher {
  def props(eventName: String, time: String) = Props(new EventWatcher(eventName, time))
}

/**
  * This actor should kill itself after the time has past
  * @param eventName
  * @param time
  */
class EventWatcher(val eventName: String, val time: String) extends Actor with ActorLogging {

  // TODO kill actor after time
  val options = mutable.Map[String, OddsTracker]()

  override def preStart() = {
    log.info(s"Started ${self.path}")
  }

  override def postStop() = {
    log.info(s"Stopped ${self.path}")
  }

  def receive: Receive = {
    case lines: Seq[SportsEventLine] =>
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
}

