package actors

import akka.actor.Actor
import concurrent.Future

import play._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import services.github.GithubWS

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import scala.concurrent.duration._
import scala.concurrent.Await

class AddForks extends Actor {
  lazy val rootId = Play.application.configuration.getLong("gist.root")
  lazy val log = play.api.Logger("application.actor")

  private def createdMax = {
    services.search.Search.maxCreated.map({ r =>
      r.right.toOption.map({ json =>
        (json \ "hits" \ "hits" \\ "created_at").headOption.map( _.as[String] )
      }).flatten
    })
  }

  private def filter( forks: Seq[JsObject], dateMax : Option[String] ) = {
    forks.filter({ json =>
      dateMax.map({ d => (json \ "created_at").as[String] > d }).getOrElse(true)
    })
  }

  def receive = {
    case "update" => {

      val response = for{
        dateMax <- createdMax
        forks <- GithubWS.Gist.listForks(rootId)
        response <- Future.sequence( filter(forks, dateMax).map( json => services.search.Search.insert(json) ) )
      }yield( response )

      log.debug( Await.result( response, 50 seconds ).map{ r =>
        r.fold(
          err => err.json \ "error",
          r => r
        )
      }.toString )
    }
  }

}