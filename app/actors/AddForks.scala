package actors

import akka.actor.Actor
import concurrent.Future

import play._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import services.GistBlackList
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

  private def logResponse( response: Seq[Either[String, JsValue]] ) = {
    log.debug(
      response.map(_.fold(
        err => err,
        r => r
      )).toString
    )
  }

  private def insert( gists: Seq[JsObject] ): Future[Seq[Either[String, JsValue]]] = {
    play.Logger.debug("Inserting %s gists".format(gists.size))
    Future.sequence(
      gists.map{ json =>
        services.search.Search.insert(json).map{ response =>
          response.left.map{ r => "Failed to index gist, %s - %s".format(r.status, r.body) }
        } recover {
          case e: Exception => {
            play.Logger.error("FAILURE recover : %s".format(e.getMessage));
            Left("Failed to index gist, %s".format(e.getMessage))
          }
        }
      }
    )
  }

  def receive = {
    case "update" => {
      (for{
        lastCreated     <- extractField(services.search.Search.lastCreated, "created_at")
        lastUpdated     <- extractField(services.search.Search.lastUpdated, "updated_at" )
        forksId         <- GithubWS.Gist.listNewForks(rootId, lastCreated, lastUpdated)
        blacklistId     <- GistBlackList.ids
        whiteIds = forksId -- blacklistId
        forks           <- GithubWS.Gist.fetchForks(whiteIds)
        stars           <- GithubWS.Gist.fetchStars(whiteIds)
        response        <- insert(forks)
      } yield (response)).map{ r =>
        logResponse(r)
      } recover {
        case e: Exception => play.Logger.error("Failed to update the gists : %s".format(e.getMessage) )
      }

    }
  }

}
