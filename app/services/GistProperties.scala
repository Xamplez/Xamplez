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

	def update[T](key: String, f: Option[T] => T )(implicit format: Format[T]): Either[String, T] =
		updateO(key, { v: Option[T] => Some(f(v)) })(format).right.map(_.get)

	def updateO[T](key: String, f: Option[T] => Option[T] )(implicit format: Format[T]): Either[String, Option[T]] = {
		gistId.map{ id =>  synchronized {
			val updated = Gist.getFile(id, fileName).map{ s =>
				val json = s.map{ str =>
					Json.parse(str).validate[Option[JsObject]].fold(
						invalid => {
							play.Logger.warn(s"Failed to load data, invalid format, will be overriden: $str")
							None
						},
						json => json
					)
				}.getOrElse(None)

				val value = json match {
					case Some(js) => f( (js \ key).as[Option[T]] )
					case _ => f(None)
				}
				val merged = (json, value) match {
					case ( Some(js), Some(value) ) => Some(js ++ Json.obj( key -> value ))
					case ( None, Some(value) ) => Some(Json.obj( key -> value ))
					case ( Some(js), None ) => Some(js - key)
					case ( None, None ) => None
				}

				merged match {
					case Some(js) => Gist.putFile(id, fileName, Json.prettyPrint(js)).map{ r =>
						r.status match {
							case 200 => value
							case s => Future.failed(new RuntimeException("Failed to update file : %s - %s".format(s, r.body))); None
						}
					}
					case None => Future(None)
				}
			}.flatMap(identity)

			Right( Await.result( updated, 30 seconds ) )
		}}.getOrElse{
			Left("Failed to fetch key : missing key config.external.gist")
		}
	}
}

object GistProperties extends GistBackedProperties

object GistConfig {
	import scala.collection.JavaConverters._
	import com.typesafe.config._
	import ConfigValueType._

	def convert(cv: ConfigValue): JsValue = cv.valueType match {
		case STRING  => JsString(cv.unwrapped.asInstanceOf[String])
		case BOOLEAN => JsBoolean(cv.unwrapped.asInstanceOf[Boolean])
		case NULL    => JsNull
		case NUMBER  => JsNumber(BigDecimal(cv.unwrapped.toString))
		case OBJECT  => cv.asInstanceOf[java.util.Map[String, ConfigValue]].asScala.foldLeft(Json.obj()){
			case (obj, (k,v)) => obj + (k-> convert(v))
		}
		case LIST    => cv.asInstanceOf[java.util.List[ConfigValue]].asScala.foldLeft(Json.arr()){
			case (arr, v) => arr :+ convert(v)
		}
	}

	implicit val configValueWriter = Writes[com.typesafe.config.ConfigValue]( cv => convert(cv) )

	def getConfigAsJson(field: String): Option[JsObject] = {
		Play.application.configuration.getObject(field).map(Json.toJson(_).as[JsObject])
	}
}