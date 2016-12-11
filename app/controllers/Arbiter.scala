package controllers

// external
import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

// Internal
import services.DBService


class Arbiter @Inject() (val database: DBService,
                         val messagesApi: MessagesApi,
                         implicit val webJarAssets: WebJarAssets)
  extends Controller with AuthConfigTrait with OptionalAuthElement with I18nSupport  {


  /**
    * Post new sportsbook events here.
    */
  def addEvents() = Action.async { implicit request =>
    println(request.body.asJson)
    Future.successful(Ok(""))
  }
}
