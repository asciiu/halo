package services.actors

// external
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef}
import common.models.halo.{BookOdds, EventData}
import models.sports.SportMatrix.{SendAllEvents, SendEventOdds, SendShiftedEvents}
import models.sports.{OddsMatrixAB, SportMatrix, SportsBookOdds}
import play.api.Configuration

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// internal
import models.sports.SportsBookData
import services.DBService


object Matrix {

  case class Gleam(depth: Int)
  case class AllEvents(filter: Option[String])
  case class EventOdds(eventID: String)
  case class ShiftedEvents(filter: Option[String], count: Option[Int])
}

/**
  * This actor is reponsible for managing all sports books.
  */
class Matrix @Inject()(val database: DBService, conf: Configuration)
                         (implicit ctx: ExecutionContext) extends Actor with ActorLogging {
  import Matrix._

  // e.g. NFL Football -> matrix actor of events for sport
  val matrices = mutable.Map[String, ActorRef]()

  override def preStart() = {
    log.info("Started matrix")
    context.system.eventStream.subscribe(self, classOf[SportsBookData])
  }

  override def postStop() = {
    log.info("Stopped matrix")
    context.system.eventStream.unsubscribe(self, classOf[SportsBookData])
  }

  def receive = {
    case data: SportsBookData =>
      update(data)
    case Gleam(depth) =>
      //sender ! gleam(depth)
    case AllEvents(filterOpt) =>

      filterOpt match {
        case Some(str) =>
          matrices.keys.find( _.contains(str)) match {
            case Some(key) =>
              matrices(key) ! SendAllEvents(sender)
            case None =>
              sender ! List[(String, OddsMatrixAB)]()
          }
        case None =>
          if (matrices.nonEmpty) {
            // defaults to head sport
            matrices.head._2 ! SendAllEvents(sender)
          } else {
            sender ! List[(String, OddsMatrixAB)]()
          }
      }
    case EventOdds(eventID) =>
      getEventOdds(eventID)

    case ShiftedEvents(filterOpt, countOpt) =>
      filterOpt match {
        case Some(str) =>
          matrices.keys.find(_.contains(str)) match {
            case Some(key) =>
              matrices(key) ! SendShiftedEvents(sender, countOpt)
            case None =>
              sender ! List[(String, OddsMatrixAB)]()
          }
        case None =>
          if (matrices.nonEmpty) {
            // defaults to head sport
            matrices.head._2 ! SendShiftedEvents(sender, countOpt)
          } else {
            sender ! List[(String, OddsMatrixAB)]()
          }
      }
  }

  private def getEventOdds(eventID: String) = {
    val prefix = eventID.substring(0, 3)
    matrices.keys.find( _.startsWith(prefix)) match {
      case Some(key) =>
        matrices(key) ! SendEventOdds(eventID, sender)
      case None =>
        sender ! EventData("NA", "NA", List[BookOdds]())
    }
  }

  private def update(data: SportsBookData) = {
    val sportname = data.sport
    val bookname = data.bookname

    // get matrix with sport name
    // example NBA Basketball
    val matrix = matrices.get(sportname) match {
      case Some(m) => m
      case None =>
        val nref = context.actorOf(SportMatrix.props(sportname), name = sportname.replace(" ", ""))
        matrices += sportname -> nref
        nref
    }

    matrix ! data
  }
}

