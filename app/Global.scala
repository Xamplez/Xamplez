
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
  // to load the config from Gist without reaching Github limits, we need to be sure
  // to have Oauth Id/Secret
  val client_id     = sys.props.get("XAMPLEZ_GH_ID")
  val client_secret = sys.props.get("XAMPLEZ_GH_SECRET")
  assert(client_id.isDefined)
  assert(client_secret.isDefined)

  override def onLoadConfig( conf: Configuration, path: java.io.File, cl: ClassLoader, mode: Mode.Mode) = {
    val url = ( conf.getLong("config.external.gist"), conf.getString("config.external.url") ) match {
      case (Some(id), _ ) =>{
        try{
          Await.result(Gist.getFileUrl(id, "application.conf", authenticated=true, client_id, client_secret), 10.seconds)
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

    val addForks = Akka.system.actorOf(Props(new actors.AddForks), name = "addForks")
    Akka.system.scheduler.schedule(0 seconds, 5 minutes, addForks, "indexing")

  }

  override def onStop(app: Application) {
    Search.stop();
  }

}