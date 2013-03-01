import play.api._
import play.api.mvc._

import services.search._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    play.Logger.info("Starting")
    Search.start();
  }
}