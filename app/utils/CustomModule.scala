package utils

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.actors.Exchange

class CustomModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[Exchange]("exchange")
  }
}
