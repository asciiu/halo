package services.actors

// external
import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject

import models.sports.OddsMatrix2
import play.api.Configuration

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// internal
import models.sports.{ SportsEvent, SportsBookData, SportsBookOdds }
import services.DBService
import SportsBookOdds._


/**
  * This actor is reponsible for managing all sports books.
  */
class Bookie @Inject()(val database: DBService, conf: Configuration)
                      (implicit ctx: ExecutionContext) extends Actor with ActorLogging {

  val sports = mutable.Map[String, String]()

  override def preStart() = {
    log.info("Started the exchanger")
  }

  override def postStop() = {
    log.info("Stopped the exchanger")
  }

  def receive = {
    case sportsBookData: SportsBookData =>

      val bookname = sportsBookData.exchange
      val sport = sportsBookData.sport
      val events = sportsBookData.events

      events.foreach( e => extractML(e))
  }

  def extractML(sportsevent: SportsEvent) = {
    val eventName = sportsevent.name
    val lines = sportsevent.lines

    val mlOdds = lines.filter( l => l.name.endsWith("ML"))

    if (mlOdds.length == 2) {
      val p1 = mlOdds(0).name.replace(" ML", "")
      val p2 = mlOdds(1).name.replace(" ML", "")

      if (p1 < p2) {
        new OddsMatrix2(p1, p2)
      } else {
        new OddsMatrix2(p2, p1)
      }

      val odds = SportsBookOdds("somebook", mlOdds(0), mlOdds(1))
    }
  }
}

