package models.sports

// external
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.sports.analytics.Sport

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// internal


object Bookmaker {
  def props(name: String) = Props(new Bookmaker(name))

  case class SendSportNames(out: ActorRef)
  case class SendAllData(out: ActorRef)
}

/**
  * This actor will represent all activity under a single bookmaker.
  */
class Bookmaker (val name: String)(implicit ctx: ExecutionContext) extends Actor with ActorLogging {
  import Bookmaker._

  // maps sportname to the actor that manages that sport
  //val sports = mutable.Map[String, ActorRef]()
  val sports = mutable.Map[String, Sport]()

  override def preStart() = {
    log.info(s"Started ${self.path}")
  }

  override def postStop() = {
    log.info(s"Stopped ${self.path}")
  }

  def receive = {
    case data: SportsBookData =>
      val sportName = data.sport

      val sport = sports.get (sportName) match {
        case Some(s) => s
        case None =>
          //val newRef = context.actorOf(SportBook.props(sportName), name = sportName.replace(" ", ""))
          val newSport = new Sport(sportName)
          sports += sportName -> newSport
          newSport
      }

      val updated = sport.update(data.events)
      if (updated.length > 0) {
        val newData = data.copy(events = updated)

        // publish update to system subscribers
        context.system.eventStream.publish(newData)
      }

    /**
      * returns all current data for this bookmaker
      */
    case SendAllData(out) =>
      out ! allData()

    /**
      * returns name of all sports as
      * list of Strings
      */
    case SendSportNames(out) =>
      out ! sports.keys.toList

  }

  private def allData(): List[SportsData] = {
    sports.values.map(_.sportData).toList
  }
}

