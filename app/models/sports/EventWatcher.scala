package models.sports

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}

import scala.collection.mutable

object EventWatcher {
  def props(eventName: String, time: String) = Props(new EventWatcher(eventName, time))

  case class GetCurrentOdds(optionName: String)
}

/**
  * This actor should kill itself after the time has past
  * @param eventName
  * @param time
  */
class EventWatcher(val eventName: String, val time: String) extends Actor with ActorLogging {
  import EventWatcher._

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
    case GetCurrentOdds(optionName) =>
      sender ! getCurrentOdds(optionName)
  }

  private def getCurrentOdds(optionName: String): Double = {
    options.get(optionName) match {
      case Some(tracker) => tracker.currentOdds
      case None => 0.0
    }
  }
}

