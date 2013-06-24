
import akka.actor.Props
import com.typesafe.config.ConfigFactory;
import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import scala.concurrent._
import scala.concurrent.duration._

import services.search._
import services.github.GithubWS._

object Global extends GlobalSettings {

  override def onLoadConfig( conf: Configuration, path: java.io.File, cl: ClassLoader, mode: Mode.Mode) = {
    val url = ( conf.getLong("config.external.gist"), conf.getString("config.external.url") ) match {
      case (Some(id), _ ) =>{
        try{
          Some(Await.result(Gist.getFileUrl(id, "application.conf", false), 10.seconds))
        }catch{
          case e: Exception => {
            play.Logger.error("Failed to load file application.conf from gist : %s; %s".format(id, e.getMessage))
            None
          }
        }
      }
      case ( _, u ) => u
      case _ => None
    }

    url.map{ u =>
      val config = ConfigFactory.parseURL( new java.net.URL(u) );
      Configuration(config.withFallback(conf.underlying));    
    }.getOrElse(conf)
  }

  override def onStart(app: Application) {
    Search.start;

    val addForks = Akka.system.actorOf(Props(new actors.AddForks), name = "addForks")
    Akka.system.scheduler.schedule(0 seconds, 1 minutes, addForks, "update")

  }

  override def onStop(app: Application) {
    Search.stop();
  }

}