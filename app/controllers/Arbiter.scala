package controllers

// external
import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import jp.t2v.lab.play2.auth.OptionalAuthElement
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

// Internal
import models.sports._
import services.DBService


class Arbiter @Inject() (val database: DBService,
                         val messagesApi: MessagesApi,
                         @Named("bookie") bookie: ActorRef,
                         implicit val webJarAssets: WebJarAssets)
  extends Controller with AuthConfigTrait with OptionalAuthElement with I18nSupport  {

  implicit val oddsRead = Json.reads[SportsEventOdds]
  implicit val lineRead = Json.reads[SportsEventLine]
  implicit val eventRead = Json.reads[SportsEvent]
  implicit val bookReads = Json.reads[SportsBook]

  /**
    * Post new sportsbook events here.
    */
  def addEvents() = Action.async( parse.json ) { implicit request =>
    request.body.validate[SportsBook].fold (
      errors => {
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      newBook => {
        bookie ! newBook
        Future.successful(Ok(Json.obj("status" ->"OK", "message" -> ("Received") )))
      }
    )
  }
}
