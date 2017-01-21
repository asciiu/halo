package models.sports

// external
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

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
  // thought: should you map eventID to a SportEvent object?
  // the bad thing will be the lookups during the receive of SportsBookData

  // lookup by eventID
  //val sportingEvents = mutable.Map[Int, SportEvent]()

  // lookup by eventName
  val sportingEventNames = mutable.Map[String, Int]()


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

      // update each event
      for (event <- events) {
        val eventName = event.name
        val normalizeName = event.name.split(" vs ").sorted.mkString(" vs ")

        //sportingEventNames.find(eventName)

        println(eventName)


        // sport event shall have an id and a name

        // sport event should have an object that keeps track of the
        // different odds

        // each odds should be tracked over time
      }

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

  //private def allData(): List[SportsData] = {
  //  sportingEvents.values.map(_.sportData).toList
  //}
}

