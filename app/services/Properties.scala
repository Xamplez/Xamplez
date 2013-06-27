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

trait GistBackedProperties{
	lazy val gistId = Play.application.configuration.getLong("config.external.gist")
	lazy val fileName = Play.application.configuration.getString("config.external.properties").getOrElse("properties")

	def get[T](key: String )(implicit read: Reads[T]): Future[Option[T]] = {
		gistId.map{ id =>
			Gist.getFile(id, fileName).map{ str =>
				str.map{ u => Json.parse(u).asOpt[T]( (__ \ key).read[T] ) }.getOrElse(None)
			}
		}.getOrElse{
			play.Logger.warn("Failed to fetch key : missing key config.external.gist")
			Future.failed(new RuntimeException("Failed to fetch key : missing key config.external.gist"))
		}
	}

	def set[T](key: String, value: T )(implicit write: Writes[T]): Future[T] = {
		gistId.map{ id =>
			Gist.getFile(id, fileName).map{ s => s.map{ str =>
				Json.parse(str).validate[Option[JsObject]].fold(
					invalid => {
						play.Logger.warn(s"Failed to load data, invalid format, will be overriden: $str")
						None
					},
					json => json
				)
			}}.map{ json =>
				val merged = json match{
					case Some(Some(js)) => js ++ Json.obj( key -> value)
					case _ => Json.obj( key -> value)
				}

				Gist.putFile(id, fileName, Json.prettyPrint(merged)).map{ _ => value }
			}.flatMap(identity)
		}.getOrElse{
			Future.failed(new RuntimeException("Failed to fetch key : missing key config.external.gist"))
		}
	}

	def remove[T](key: String )(implicit read: Reads[T]): Future[Option[T]] = {
		gistId.map{ id =>
			Gist.getFile(id, fileName).map{ str =>
				str.map{ u =>
					Json.parse(u).asOpt[JsObject].map{ obj =>
						val content = Json.prettyPrint(obj - key)
						Gist.putFile(id, fileName, content).map{ _ =>
							obj.asOpt[T]( (__ \ key).read[T] )
						}
					}.getOrElse( Future[Option[T]](None) )
				}.getOrElse( Future[Option[T]](None) )
			}.flatMap(identity)
		}.getOrElse{
			Future.failed(new RuntimeException("Failed to fetch key : missing key config.external.gist"))
		}
	}
}

object Properties extends GistBackedProperties