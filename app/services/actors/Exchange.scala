package services.actors

// external
import akka.actor.{Actor, ActorLogging, ActorRef}
import javax.inject.Inject
import play.api.Configuration
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// internal
import models.sports.Bookmaker
import models.sports.{ SportsBookData, SportsBookOdds }
import services.DBService
import SportsBookOdds._


/**
  * This actor is reponsible for managing all sports books.
  */
class Exchange @Inject()(val database: DBService, conf: Configuration)
                   (implicit ctx: ExecutionContext) extends Actor with ActorLogging {

  val bookmakers = mutable.Map[String, ActorRef]()

  override def preStart() = {
    log.info("Started exchanger")
  }

  override def postStop() = {
    log.info("Stopped exchanger")
  }

  def receive = {
    case sportsBookData: SportsBookData =>
      val bookname = sportsBookData.bookname

      val ref = bookmakers.get(bookname) match {
        case Some(maker) => maker
        case None =>
          val nef = context.actorOf(Bookmaker.props(bookname), name = bookname.replace(" ", ""))
          bookmakers += bookname -> nef
          nef
      }
      ref ! sportsBookData
  }
}

