package services.search

import org.joda.time.DateTime
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

import models._

import scala.util.matching._

trait EsAPI{

  val (isEmbedded, clusterName, elasticUrl, indexName)  = {
    val config = Play.application.configuration
    (
      config.getBoolean("elasticsearch.embedded").getOrElse(true),
      config.getString("elasticsearch.clustername").getOrElse("xamplez"),
      config.getString("elasticsearch.url").getOrElse("http://localhost:9200"),
      config.getString("elasticsearch.index").getOrElse("xamplez")
    )
  }
  lazy val INDEX_URL = s"$elasticUrl/$indexName"

  protected def getIndex(name: String): Future[Either[String, JsObject]] = {
    WS.url(s"$elasticUrl/$name/_settings")
      .get()
      .map { r =>
        if(r.status == 200) Right(r.json.as[JsObject])
        else Left(s"status:${r.status} statusText:${r.statusText} error:${r.body}")
      }
  }

  protected def createIndex(name: String): Future[Either[String, JsObject]] = {
    WS.url(s"$elasticUrl/$name")
      .put(play.api.mvc.Results.EmptyContent())
      .map { r =>
        if(r.status == 200) Right(Json.obj("res" -> "ok"))
        else Left(s"status:${r.status} statusText:${r.statusText} error:${r.body}")
      }
  }

  protected def insert(typeName: String, id: String, value: JsValue): Future[Either[String, JsValue]] = {
    play.Logger.debug(s"ES : inserting $typeName $id : $value")
    WS.url(s"$INDEX_URL/$typeName/$id")
      .put(value)
      .map { r =>
        if(r.status == 200 || r.status == 201) Right(r.json)
        else Left(s"Couldn't insert $typeName with $id (status:${r.status} msg:${r.statusText}")
      }
  }

  protected def get(typeName: String, id: String): Future[Either[String, JsValue]] = {
    play.Logger.debug(s"ES : fetching $typeName $id")
    WS.url(s"$INDEX_URL/$typeName/$id")
      .get
      .map { r =>
        if(r.status == 200 || r.status == 201) Right(r.json)
        else Left(s"Couldn't find $typeName with $id (status:${r.status} msg:${r.statusText}")
      }
  }

  protected def delete(typeName: String, id: String): Future[Response] = {
    play.Logger.debug(s"Search : deleting $typeName with id : $id")
    WS.url(s"$INDEX_URL/$typeName/$id").delete
  }

}