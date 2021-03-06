package controllers

// external
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}
import javax.inject.{Inject, Named}

import jp.t2v.lab.play2.auth.OptionalAuthElement
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import services.actors.Matrix.{AllEvents, EventOdds, ShiftedEvents}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// Internal
import common.models.halo.{BookOdds, EventData, TimedPoint}
import models.sports._
import services.DBService
import services.actors.EventCoordinator.AllSportNames
import services.actors.Matrix.Gleam


class Arbiter @Inject() (val database: DBService,
                         val messagesApi: MessagesApi,
                         val normalizer: Normalizer,
                         @Named("coordinator") coordinator: ActorRef,
                         @Named("matrix") matrix: ActorRef,
                         implicit val webJarAssets: WebJarAssets)
  extends Controller with AuthConfigTrait with OptionalAuthElement with I18nSupport  {

  implicit val timeout = Timeout(5 seconds)

  /**
    * Post new sportsbook events here.
    */
  def addEvents() = Action.async( parse.json ) { implicit request =>
    request.body.validate[SportsBookData].fold (
      errors => {
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      newBook => {
        normalizer.process(newBook).map { data =>
          matrix ! data
          Ok(Json.obj("status" ->"OK", "message" -> ("Received") ))
        }
      }
    )
  }

  /**
    * Should display a view of betting lines with odds for a specific sport.
    */
  def matrices(filter: Option[String]) = AsyncStack { implicit request =>
    (matrix ? AllEvents(filter)).mapTo[List[(String, OddsMatrixAB)]].map { list =>
      // show ML only?
      //val mlMatrices = list.filter(_._1.endsWith("ML"))
      Ok(views.html.arbiter.grid(loggedIn, list))
    }
  }

  /**
    * Should display a view of betting lines with odds for a specific sport. Where the bettings lines
    * have moved at least once.
    */
  def definite(filter: Option[String], count: Option[Int]) = AsyncStack { implicit request =>
    (matrix ? ShiftedEvents(filter, count)).mapTo[List[(String, OddsMatrixAB)]].map { list =>
      // show ML only?
      //val mlMatrices = list.filter(_._1.endsWith("ML"))
      Ok(views.html.arbiter.grid(loggedIn, list))
    }
  }


  /**
    * TODO remove this shit when working proto.
    */
  def mockData(bookname: String): BookOdds =  {
    val test = LocalDateTime.now()
    val seconds = test.toEpochSecond(ZoneOffset.UTC)

    val bookpts = collection.mutable.ListBuffer[TimedPoint]()
    val time = OffsetDateTime.now()
    (0 to 12).foreach { i =>
      val t = time.plusMinutes(i * 5)
      val newPoint = TimedPoint(t.toEpochSecond(), Math.random() + 1, Math.random()+1)
      bookpts.append(newPoint)
    }
    BookOdds(bookname, bookpts.toList)
  }


  /**
    * All odds across mutiple books for a single event.
    * @param eventID
    * @return json EventData object
    */
  def sportEventOdds(eventID: String) = AsyncStack { implicit request =>
    // get odds from matrix
    (matrix ? EventOdds(eventID)).mapTo[EventData].map { result =>
      Ok(upickle.default.write[EventData](result))
    }

    // TODO remove this test stuff
//    val nitro = mockData("Nitro")
//    val betcoin = mockData("Betcoin")
//    val cloudbet = mockData("Cloudbet")
//    val eventTime = LocalDateTime.now()
//
//    val data = EventData(eventID.toString, eventTime.toString, List(nitro, betcoin, cloudbet))
//
//    Future.successful(Ok(upickle.default.write[EventData](data)))
  }

  /**
    * @param eventID
    * @return
    */
  def event(eventID: String) = AsyncStack { implicit request =>
    Future.successful(Ok(views.html.arbiter.event(loggedIn, eventID)))
  }


  /**
    * The route for retrieving all sport names
    * @return ok status of 200 with JSON array of sport names
    */
  def allSportNames() = AsyncStack { implicit request =>
    (coordinator ? AllSportNames).mapTo[List[String]].map { names =>
      Ok(upickle.default.write(names))
    }
  }
}
