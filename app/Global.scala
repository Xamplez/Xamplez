
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
          Await.result(Gist.getFileUrl(id, "application.conf", conf.getString("github.client.id"), conf.getString("github.client.secret")), 10.seconds)
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
      play.Logger.info("loading remote config from url: "+u)
      val config = ConfigFactory.parseURL( new java.net.URL(u) );
      Configuration(config.withFallback(conf.underlying));
    }.getOrElse(conf)
  }

  override def onStart(app: Application) {
    Search.start;

    val indexer = Akka.system.actorOf(Props(new actors.Indexer), name = "indexer")
    Akka.system.scheduler.schedule(0 seconds, 5 minutes, indexer, "indexing")

    //Akka.system.scheduler.schedule(1 minutes, 5 minutes, indexer, "updating")
  }

  override def onStop(app: Application) {
    Search.stop();
  }

}