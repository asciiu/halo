package utils

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.actors.{EventCoordinator, Matrix}

class CustomModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[EventCoordinator]("coordinator")
    bindActor[Matrix]("matrix")
  }
}
