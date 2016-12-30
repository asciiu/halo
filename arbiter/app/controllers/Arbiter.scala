package controllers

// external
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.{Inject, Named}

import jp.t2v.lab.play2.auth.OptionalAuthElement
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
