package utils

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.actors.Bookie

class CustomModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[Bookie]("bookie")
  }
}
