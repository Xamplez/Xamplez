package actors

import akka.actor.Actor
import concurrent.Future

import play._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import services.github.GithubWS

import scala.concurrent.duration._
import scala.concurrent.Await

class AddForks extends Actor {
  lazy val rootId = Play.application.configuration.getLong("gist.root")
  lazy val log = play.api.Logger("application.actor")

  var lastRun : String = ""


  def receive = {
    case "update" => {
      val response = for{
        forks <- GithubWS.Gist.listForks(rootId)
        response <- Future.sequence( forks.map( json => services.search.Search.insert(json) ) )
      }yield( response )

      log.debug( Await.result( response, 10.seconds ).map{ r =>
        r.fold(
          err => err.json \ "error",
          r => r
        ) 
      }.toString )
    }
  }

}