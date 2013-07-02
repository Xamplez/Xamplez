package services

import scala.concurrent._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.json.syntax._
import play.api.libs.functional.syntax._
import play.api.libs.json.extensions._
import play.api.libs.ws._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import services.github.GithubWS._
import services.search.Search

trait GistBlackListService{

	def ids: Future[Set[Long]] = {
		GistProperties.get[Set[String]]("blacklist").map { ids =>
		 	ids.getOrElse(Set.empty).map( _.toLong )
		} recover {
			case e: Exception => {
				play.Logger.warn("Failed to load blacklist : %s".format(e.getMessage))
				Set.empty
			}
		}
	}

	def add(id: Long): Future[Response] = {
		for{
			ids 	 <- ids
			response <- Search.delete(id)
			removed  <- GistProperties.set("blacklist", (ids + id).map(_.toString))
		}yield( response )
	}

}

object GistBlackList extends GistBlackListService