package models.sports

// external
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.sports.analytics.Sport

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// internal


object SportingEventOrganizer {
  def props(sportName: String) = Props(new SportingEventOrganizer(sportName))
}

/**
  * This actor will organize all events for a single sport.
  */
class SportingEventOrganizer(val sportName: String)(implicit ctx: ExecutionContext) extends Actor with ActorLogging {
  import SportingEventOrganizer._

  // maps an event name to an object that manages the event
  //val sports = mutable.Map[String, ActorRef]()
  val sportingEvents = mutable.Map[String, Sport]()

  override def preStart() = {
    log.info(s"Started ${self.path}")
  }

  override def postStop() = {
    log.info(s"Stopped ${self.path}")
  }

  def receive = {
    case data: SportsBookData =>
      val bookName = data.bookname
      val events = data.events

      //val sport = sportingEvents.get (sportName) match {
      //  case Some(s) => s
      //  case None =>
      //    //val newRef = context.actorOf(SportBook.props(sportName), name = sportName.replace(" ", ""))
      //    val newSport = new Sport(sportName)
      //    sportingEvents += sportName -> newSport
      //    newSport
      //}

      //val updated = sport.update(data.events)
      //if (updated.length > 0) {
      //  val newData = data.copy(events = updated)

      //  // publish update to system subscribers
      //  context.system.eventStream.publish(newData)
      //}
  }

  private def allData(): List[SportsData] = {
    sportingEvents.values.map(_.sportData).toList
  }
}

