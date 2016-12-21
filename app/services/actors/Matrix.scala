package services.actors

// external
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import play.api.Configuration
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// internal
import models.sports.SportsBookData
import services.DBService


object Matrix {

}

/**
  * This actor is reponsible for managing all sports books.
  */
class Matrix @Inject()(val database: DBService, conf: Configuration)
                         (implicit ctx: ExecutionContext) extends Actor with ActorLogging {
  import Matrix._

  override def preStart() = {
    log.info("Started matrix")
    context.system.eventStream.subscribe(self, classOf[SportsBookData])
  }

  override def postStop() = {
    log.info("Stopped matrix")
    context.system.eventStream.unsubscribe(self, classOf[SportsBookData])
  }

  def receive = {
    case sportsBookData: SportsBookData =>
      log.info(sportsBookData.toString)
  }
}

