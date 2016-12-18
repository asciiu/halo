package models.sports

// external
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// internal


object Bookmaker {
  def props(name: String) = Props(new Bookmaker(name))
}

/**
  * This actor will represent all activity under a single bookmaker.
  */
class Bookmaker (val name: String)(implicit ctx: ExecutionContext) extends Actor with ActorLogging {

  val sports = mutable.Map[String, ActorRef]()

  override def preStart() = {
    log.info(s"Started ${self.path}")
  }

  override def postStop() = {
    log.info(s"Stopped ${self.path}")
  }

  def receive = {
    case data: SportsBookData =>
      val sportName = data.sport

      val ref = sports.get (sportName) match {
        case Some(actor) => actor
        case None =>
          val newRef = context.actorOf(SportBook.props(sportName), name = sportName.replace(" ", ""))
          sports += sportName -> newRef
          newRef
      }
      ref ! data.events
  }
}

