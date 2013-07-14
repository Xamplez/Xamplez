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

import scala.collection.JavaConverters._

class Indexer extends Actor {
  lazy val rootIds = Play.application.configuration.getLongList("gist.roots").asScala
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

  val fetcher =
    Akka.system.actorOf(Props[GistActor]
          .withRouter(RoundRobinRouter(nrOfInstances = 5)))

  val inserter =
    Akka.system.actorOf(Props[ElasticSearchActor]
          .withRouter(RoundRobinRouter(nrOfInstances = 5)))

  implicit val timeout = Timeout(600 seconds)

  private def insert( ids: Set[Long], action: String ): Future[Seq[(Long, Either[String, JsValue])]] = {
    log.debug(s"Fetching ${ids.size} forks for $ids...")

    Future.sequence(
      ids.toSeq.map{ id => 
        (for{
          fork    <- (fetcher ? FetchGist(id)).mapTo[Option[JsObject]]
          stars   <- (fetcher ? FetchStar(id)).mapTo[JsObject]
        } yield (fork, stars, action)).flatMap{
          case (Some(fork), stars, "insert") => services.search.Search.insert(fork ++ stars).map( r => id -> r )
          case (Some(fork), stars, "update") => services.search.Search.update(id, fork, stars).map( r => id -> r )
          case (_, _, _) => Future.successful(id -> Left(s"Can't $action $id because gist not found or no stars"))
        }
      }
    ) 
  }

  def receive = {
    case "indexing" => {
      log.info("Launching re-indexing")
      (for{
        lastCreated    <- extractField(services.search.Search.lastCreated, "created_at")
        lastUpdated    <- extractField(services.search.Search.lastUpdated, "updated_at" )
        forkIds        <- Future.sequence( rootIds.map{ rootId =>
                            GithubWS.Gist.listNewForks(rootId, lastCreated, lastUpdated)
                          } ).map(_.toSet.flatten)
        blacklistId    <- GistBlackList.ids
        resps          <- insert( forkIds -- blacklistId, "insert" )
      } yield (resps)).map{ r =>
        logIndexingResponse(r.toSeq)
      } recover {
        case e: Exception => play.Logger.error("Failed to index the gists : %s".format(e.getMessage) )
      }
    }

    case "updating" =>
      log.info("Launching updating")

      (for{
        forkIds        <- Future.sequence( rootIds.map{ rootId =>
                            GithubWS.Gist.forksId(rootId)
                          } ).map(_.toSet.flatten)
        blacklistId    <- GistBlackList.ids
        resps          <- insert( forkIds -- blacklistId, "update" )
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