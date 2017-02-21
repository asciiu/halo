package services.actors

// external
import akka.actor.{Actor, ActorLogging, ActorRef}
import javax.inject.Inject
import play.api.Configuration
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// internal
import models.sports.SportingEventOrganizer
import models.sports.SportsBookData
import services.DBService


object EventCoordinator {
  case object AllSportNames
}
/**
  * This actor will coordinate SportsBookData to appropriate child actors that
  * will manage events for the sport.
  */
class EventCoordinator @Inject()(val database: DBService, conf: Configuration)
                                (implicit ctx: ExecutionContext) extends Actor with ActorLogging {

  import EventCoordinator._

  // maps sport name to an actor that manages the sport's data
  // sportname example: NBA Basketball
  val sportingEvents = mutable.Map[String, ActorRef]()

  override def preStart() = {
    log.info("Started coordinator")
  }

  override def postStop() = {
    log.info("Stopped coordinator")
  }

  def receive = {
    case sportsBookData: SportsBookData =>
      coordinateSportBookData(sportsBookData)

    /**
      * returns List[String] of sport names
      */
    case AllSportNames =>
      sender ! sportingEvents.keys.toList
  }


  /**
    * Coordinate received SportBookData to the appropriate actor
    * that manages the sport.
    *
    * @param sportsBookData
    */
  def coordinateSportBookData(sportsBookData: SportsBookData) = {
    val sportname = sportsBookData.sport

    val actor = sportingEvents.get(sportname) match {
      case Some(sactor) => sactor
      case None =>
        // instantiate new actor to handle sport data
        val nef = context.actorOf(SportingEventOrganizer.props(sportname), name = sportname.replace(" ", ""))
        sportingEvents += sportname -> nef
        nef
    }
    actor ! sportsBookData
  }

  private def leactor(sportName: String): Option[ActorRef] = {
    sportingEvents.get(sportName) match {
      case Some(actor) => Some(actor)
      case None => None
    }
  }
}

