package services.actors

// external
import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject
import play.api.Configuration
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// internal
import models.sports.SportsBook
import services.DBService


/**
  * This actor is reponsible for managing all sports books.
  */
class Bookie @Inject()(val database: DBService, conf: Configuration)
                      (implicit ctx: ExecutionContext) extends Actor with ActorLogging {

  override def preStart() = {
    log.info("Started the exchanger")
  }

  override def postStop() = {
    log.info("Stopped the exchanger")
  }

  def receive =  {
    case SportsBook(bookname, sportname, events) =>
      log.info(bookname)
      log.info(sportname)
      log.info(events.toString)
  }
}


