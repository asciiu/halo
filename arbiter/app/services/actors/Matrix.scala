package services.actors

// external
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef}
import models.sports.SportMatrix.{SendAllEvents, SendEventOdds}
import models.sports.{OddsMatrixAB, SportMatrix}
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
  case class EventOdds(eventID: Int)
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

  }

//  private def gleam(depth: Int): List[(String, OddsMatrixAB)] = {
//    //matrices.map(_._2.gleam("")).toList.flatten
//  }

  private def getEventOdds(eventID: Int) = {
    matrices.foreach{ case (sportname, actor) => actor ! SendEventOdds(eventID, sender) }
  }

  private def update(data: SportsBookData) = {
    val sportname = data.sport
    val bookname = data.bookname

    // get matrix for single sport
    // example NBA Basketball
    val matrix = matrices.get(sportname) match {
      case Some(m) => m
      case None =>
        val nref = context.actorOf(SportMatrix.props(sportname), name = sportname.replace(" ", ""))
        matrices += sportname -> nref
        nref
    }

    //matrix.updateData(data)
    matrix ! data
  }
}

