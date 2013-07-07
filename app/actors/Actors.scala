package actors

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinRouter
import scala.concurrent._
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask

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

import play.libs.Akka

class Indexer extends Actor {
  lazy val rootId = Play.application.configuration.getLong("gist.root")
  lazy val log = play.api.Logger("application.actor")

  private def extractField( future: Future[Either[Response, JsValue]], fieldName: String ) = {
    future.map{ r =>
      r.right.toOption.map{ json =>
        (json \ "hits" \ "hits" \\ fieldName).headOption.map( _.as[String] )
      }.flatten
    }
  }

  private def logIndexingResponse(response: Seq[(Long, Either[String, JsValue])]) = {
    if(response.isEmpty) log.info("nothing indexed")
    else {
      val oks = response.collect{ case (id, Right(js)) => id }
      val kos = response.collect{ case (id, Left(r)) => id -> r }

      val str = s"""Elastic Search Indexing Report:
        -> Tried to indexed ${response.length} gists
        -> inserted $oks
        -> couldn't insert $kos
      """
      log.info(str)
    }
  }

  private def logUpdatingResponse(response: Seq[(Long, Either[String, JsValue])]) = {
    if(response.isEmpty) log.info("nothing updated")
    else {
      val oks = response.collect{ case (id, Right(js)) => id }
      val kos = response.collect{ case (id, Left(r)) => id -> r }

      val str = s"""Elastic Search Update Report:
        -> Tried to update ${response.length} gists
        -> updated $oks
        -> couldn't update $kos
      """
      log.info(str)
    }
  }

  // private def insert( gists: Seq[JsObject] ): Future[Seq[Either[String, JsValue]]] = {
  //   play.Logger.debug("Inserting %s gists".format(gists.size))
  //   Future.sequence(
  //     gists.map{ json =>
  //       services.search.Search.insert(json).map{ response =>
  //         response.left.map{ r => "Failed to index gist, %s - %s".format(r.status, r.body) }
  //       } recover {
  //         case e: Exception => {
  //           play.Logger.error("FAILURE recover : %s".format(e.getMessage));
  //           Left("Failed to index gist, %s".format(e.getMessage))
  //         }
  //       }
  //     }
  //   )
  // }

  val fetcher =
    Akka.system.actorOf(Props[GistActor]
          .withRouter(RoundRobinRouter(nrOfInstances = 5)))

  val inserter =
    Akka.system.actorOf(Props[ElasticSearchActor]
          .withRouter(RoundRobinRouter(nrOfInstances = 5)))

  implicit val timeout = Timeout(600 seconds)

  def receive = {
    case "indexing" => {
      log.info("Launching re-indexing")
      (for{
        lastCreated    <- extractField(services.search.Search.lastCreated, "created_at")
        lastUpdated    <- extractField(services.search.Search.lastUpdated, "updated_at" )
        forkIds        <- GithubWS.Gist.listNewForks(rootId, lastCreated, lastUpdated)
        blacklistId    <- GistBlackList.ids
        whiteIds = {
          val ids = (forkIds -- blacklistId) //.take(100)
          log.debug(s"Fetching ${ids.size} forks for $ids...")
          ids
        }

        resps          <- Future.sequence(
                            //whiteIds.map(id => (fetcher ? FetchGist(id)).mapTo[Option[JsObject]])
                            whiteIds.map{ id => 
                              (for{
                                fork    <- (fetcher ? FetchGist(id)).mapTo[Option[JsObject]]
                                stars   <- (fetcher ? FetchStar(id)).mapTo[JsObject]
                              } yield (fork, stars)).flatMap{
                                case (Some(fork), stars) => services.search.Search.insert(fork ++ stars).map( r => id -> r )
                                case (_, _)              => Future.successful(id -> Left(s"Can't insert $id because gist not found or no stars"))
                              }
                            }
                          )
        /*idForks = {
          log.debug(s"Fetching stars...")
          whiteIds.zip(forks)
        }
        forkstars      <- Future.sequence(
                            idForks.collect{
                              case (id, Some(fork)) =>
                                val maybeObj = (fetcher ? FetchStar(id)).mapTo[JsObject]
                                maybeObj.map( obj => (id -> (fork ++ obj)) )
                            }
                          )
        toInsert = {
          log.debug(s"Indexing gists...")
          forkstars
        }
        response       <- Future.sequence(
                            toInsert.collect{
                              case (id, json) =>
                                val maybeR = (inserter ? InsertES(json)).mapTo[Either[String, JsValue]]
                                maybeR.map( r => (id -> r) )
                            }
                          )*/
      } yield (resps)).map{ r =>
        logIndexingResponse(r.toSeq)
      } recover {
        case e: Exception => play.Logger.error("Failed to index the gists : %s".format(e.getMessage) )
      }

    }

    case "updating" =>
      log.info("Launching updating")

      (for{
        forkIds        <- GithubWS.Gist.forksId(rootId)
        blacklistId    <- GistBlackList.ids
        whiteIds = {
          val ids = (forkIds -- blacklistId) //.take(100)
          log.debug(s"Fetching ${ids.size} forks for $ids...")
          ids
        }
        // does update just after fetching and doesn't wait to have everything...
        // TODO : should we do the same for insert???
        resps          <- Future.sequence( whiteIds.map{ id => 
                              (for{
                                fork    <- (fetcher ? FetchGist(id)).mapTo[Option[JsObject]]
                                stars   <- (fetcher ? FetchStar(id)).mapTo[JsObject]
                              } yield (fork, stars)).flatMap{
                                case (Some(fork), stars) => services.search.Search.update(id, fork, stars).map( r => id -> r )
                                case (_, _)                 => Future.successful(id -> Left(s"Can't update $id because gist not found or no stars"))
                              }
                            }
                          )
      } yield (resps)).map{ r =>
        logUpdatingResponse(r.toSeq)
      } recover {
        case e: Exception => play.Logger.error("Failed to update the gists : %s".format(e.getMessage) )
      }

  }

}

sealed trait Payload
case class InsertES(js: JsObject) extends Payload
case class FetchGist(id: Long) extends Payload
case class FetchStar(id: Long) extends Payload

import akka.pattern.pipe

class GistActor extends Actor {
  val timeout = 60 seconds

  def receive = {
    case FetchGist(id) =>
      sender ! Await.result(GithubWS.Gist.fetchFork(id), timeout)  // pipeTo sender
    case FetchStar(id) =>
      sender ! Await.result(GithubWS.Gist.fetchStar(id), timeout)  // pipeTo sender
  }
}

class ElasticSearchActor extends Actor {
  val timeout = 60 seconds

  def receive = {
    case InsertES(json) =>
      sender ! Await.result(services.search.Search.insert(json), timeout)  // pipeTo sender
  }
}