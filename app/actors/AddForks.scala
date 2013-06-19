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
import scala.concurrent._

class AddForks extends Actor {
  lazy val rootId = Play.application.configuration.getLong("gist.root")
  lazy val log = play.api.Logger("application.actor")

  private def extractField( future: Future[Either[Response, JsValue]], fieldName: String ) = {
    future.map{ r =>
      r.right.toOption.map{ json =>
        (json \ "hits" \ "hits" \\ fieldName).headOption.map( _.as[String] )
      }.flatten
    }
  }

  private def filter( forks: Seq[JsObject], lastCreated : Option[String], lastUpdated : Option[String] ) = {
    forks.filter{ json =>
      lastCreated.map{ d => (json \ "created_at").as[String] > d }.getOrElse(true) ||
      lastUpdated.map{ d => (json \ "updated_at").as[String] > d }.getOrElse(false)
    }
  }

  private def logResponse( response: Seq[Either[Response, JsValue]] ) = {
    log.debug(
      response.map(_.fold(
        err => "Error:"+err.body,
        r => r
      )).toString
    )
  }

  def receive = {
    case "update" => {
      (for{
        lastCreated <- extractField(services.search.Search.lastCreated, "created_at")
        lastUpdated <- extractField(services.search.Search.lastUpdated, "updated_at" )
        forks       <- GithubWS.Gist.listForks(rootId)
        response    <- Future.sequence(
                         filter(forks, lastCreated, lastUpdated).map{ json =>
                           services.search.Search.insert(json)
                         }
                       )
      } yield (response)).foreach(logResponse(_))
    }
  }

}
