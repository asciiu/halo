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

  /**
    * Post new sportsbook events here.
    */
  def addEvents() = Action.async( parse.json ) { implicit request =>
    request.body.validate[SportsBookData].fold (
      errors => {
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      newBook => {
        bookie ! newBook
        Future.successful(Ok(Json.obj("status" ->"OK", "message" -> ("Received") )))
      }
    )
  }

  /**
    * Should display a view of betting lines with odds for a specific sport.
    */
  def matrices(filter: Option[String]) = AsyncStack { implicit request =>
    // test stuff
    val matrix = new SportBookMatrix("NFL Football")
    val part1 = "Cardinals"
    val part2 = "Lions"
    val bookName1 = "Nitrogen Sports"
    val matchName = s"$part1 - $part2"
    val nitroline1 = SportsEventLine(part1, 1.3)
    val nitroline2 = SportsEventLine(part2, 2.3)
    matrix.addMatchOdds(bookName1, matchName, nitroline1, nitroline2)

    val bookName2 = "Cloudbet"
    val cloudbetline1 = SportsEventLine(part1, 3.3)
    val cloudbetline2 = SportsEventLine(part2, 1.3)
    matrix.addMatchOdds(bookName2, matchName, cloudbetline1, cloudbetline2)

    val bookName3 = "Betcoin"
    val betline1 = SportsEventLine(part2, 1.1)
    val betline2 = SportsEventLine(part1, 2.0)
    matrix.addMatchOdds(bookName3, matchName, betline1, betline2)

    val bookNames = matrix.bookNames
    val matchs = matrix.matchNames
    val (keya, keyb) = matchs.map{ name =>
      val names = name.split(" - ").sorted
      (names(0), names(1))
    }.head

    val odds = matrix.matchOdds(matchs.head).sortBy(_.bookname)
    println(odds)
    val linea = odds.map(_.a)
    val lineb = odds.map(_.b)

    Future.successful(Ok(views.html.arbiter.grid(loggedIn, bookNames, keya, keyb, linea, lineb)))
  }
}
