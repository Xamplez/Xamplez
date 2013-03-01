
import akka.actor.Props

import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import scala.concurrent.duration._

import services.search._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Search.start(app);

    val addForks = Akka.system.actorOf(Props(new actors.AddForks), name = "addForks")
    Akka.system.scheduler.schedule(0 seconds, 1 minutes, addForks, "update")
  }

  override def onStop(app: Application) {
    Search.stop();
  }

}