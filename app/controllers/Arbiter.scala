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
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
        exchange ! newBook
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

    val bookName4 = "BetOnline"
    val betonline1 = SportsEventLine(part2, 8.1)
    val betonline2 = SportsEventLine(part1, 3.0)
    matrix.addMatchOdds(bookName4, matchName, betonline1, betonline2)

    val betonline3 = SportsEventLine("Tigers", 8.1)
    val betonline4 = SportsEventLine("Red Sox", 3.0)
    matrix.addMatchOdds(bookName4, "Red Sox - Tigers", betonline3, betonline4)

    val bookNames = matrix.bookNames
    val matchs = matrix.matchNames
    val (keya, keyb) = matchs.map{ name =>
      val names = name.split(" - ").sorted
      (names(0), names(1))
    }.head

    val odds = matrix.matchOdds(matchs.head).sortBy(_.bookname)
    val linea = odds.map(_.a)
    val lineb = odds.map(_.b)
    val sportName = matrix.sportName

    Future.successful(Ok(views.html.arbiter.grid(loggedIn, sportName, bookNames, keya, keyb, linea, lineb)))
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
