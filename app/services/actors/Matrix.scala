package services.actors

// external
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
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
}

/**
  * This actor is reponsible for managing all sports books.
  */
class Matrix @Inject()(val database: DBService, conf: Configuration)
                         (implicit ctx: ExecutionContext) extends Actor with ActorLogging {
  import Matrix._

  // e.g. NFL Football -> matrix of events for sport
  val matrices = mutable.Map[String, SportMatrix]()

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
      sender ! gleam(depth)
  }

  private def gleam(depth: Int): List[(String, OddsMatrixAB)] = {
    matrices.map(_._2.gleam("")).toList.flatten
  }

  private def update(data: SportsBookData) = {
    val sportname = data.sport
    val bookname = data.bookname

    // get matrix for single sport
    // example NBA Basketball
    val matrix = matrices.get(sportname) match {
      case Some(m) => m
      case None =>
        val m = new SportMatrix(sportname)
        matrices += sportname -> m
        m
    }

    matrix.updateData(data)
  }
}

