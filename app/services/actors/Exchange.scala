package services.actors

// external
import akka.actor.{Actor, ActorLogging, ActorRef}
import javax.inject.Inject

import akka.util.Timeout
import models.sports.Bookmaker.SendAllData
import models.sports.SportsData
import play.api.Configuration

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.concurrent.duration._

// internal
import models.sports.Bookmaker
import models.sports.{ SportsBookData, SportsBookOdds }
import services.DBService


object Exchange {

  case object BookmakerNames
  case class BookData(bookname: String)
}
/**
  * This actor is reponsible for managing all sports books.
  */
class Exchange @Inject()(val database: DBService, conf: Configuration)
                   (implicit ctx: ExecutionContext) extends Actor with ActorLogging {

  import Exchange._

  // maps bookmaker name to actor that persists the bookmaker data
  val bookmakers = mutable.Map[String, ActorRef]()
  implicit val timeout = Timeout(5 seconds)


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

    case BookData(bookname) =>
      bookactor(bookname) match {
        case Some(actor) => actor ! SendAllData(sender)
        case None => sender ! List[SportsData]()
      }

    /**
      * returns a list of strings
      */
    case BookmakerNames =>
      sender ! bookmakers.keys.toList
  }

  private def bookactor(bookname: String): Option[ActorRef] = {
    bookmakers.get(bookname) match {
      case Some(maker) => Some(maker)
      case None => None
    }
  }
}

