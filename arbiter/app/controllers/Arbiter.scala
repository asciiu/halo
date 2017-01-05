package controllers

// external
import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.{Inject, Named}

import jp.t2v.lab.play2.auth.OptionalAuthElement
import common.models.halo.{BookOdds, BookPoint, EventData}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import services.actors.Exchange.{BookData, BookmakerNames}
import services.actors.Matrix.Gleam

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// Internal
import models.sports._
import services.DBService


class Arbiter @Inject() (val database: DBService,
                         val messagesApi: MessagesApi,
                         @Named("exchange") exchange: ActorRef,
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
        exchange ! Normalizer.process(newBook)
        Future.successful(Ok(Json.obj("status" ->"OK", "message" -> ("Received") )))
      }
    )
  }

  /**
    * Should display a view of betting lines with odds for a specific sport.
    */
  def matrices(filter: Option[String]) = AsyncStack { implicit request =>
    (matrix ? Gleam(20)).mapTo[List[(String, OddsMatrixAB)]].map { list =>
      val mlMatrices = list.filter(_._1.endsWith("ML"))
      Ok(views.html.arbiter.grid(loggedIn, list))
    }
  }

  def sportMatchData(eventName: String) = AsyncStack { implicit request =>
    // require name
    val test = LocalDateTime.now()
    val seconds = test.toEpochSecond(ZoneOffset.UTC)

    val bookpts = collection.mutable.ListBuffer[BookPoint]()
    val time = OffsetDateTime.now()
    (0 to 12).foreach { i =>
      val t = time.plusMinutes(i * 5)
      println (t.toString)
      val newPoint = BookPoint(t.toEpochSecond(), Math.random() + 1, Math.random()+1)
      bookpts.append(newPoint)
    }
    val bookOdds = BookOdds("Nitro", bookpts.toList)
    val data = EventData(eventName, List(bookOdds))

    // bookname1 with bookname odds for name
    // bookname2 with bookname odds for name
    // bookname3 with bookname odds for name

    //Future.successful(Ok(Json.toJson(data)))
    Future.successful(Ok(upickle.default.write[EventData](data)))
  }

  def sportmatch(eventName: String) = AsyncStack { implicit request =>
    Future.successful(Ok(views.html.arbiter.sportmatch(loggedIn, eventName)))
  }

  def bookmaker(bookname: String) = AsyncStack { implicit request =>
    (exchange ? BookData(bookname)).mapTo[List[SportsData]].map { list =>
      if (list.nonEmpty) {
        Ok(views.html.arbiter.bookmaker(loggedIn, list))
      } else {
        NotFound(bookname)
      }
    }
  }

  def bookmakerNames() = AsyncStack { implicit request =>
    (exchange ? BookmakerNames).mapTo[List[String]].map { names =>
      Ok(names.toString)
    }
  }
}
