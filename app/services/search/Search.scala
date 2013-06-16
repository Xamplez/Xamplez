package services.search

import scala.concurrent._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import models._

trait ElasticSearch {

  import org.elasticsearch.node._
  import org.elasticsearch.node.NodeBuilder._

  lazy val IS_EMBEDDED =
    Play.application.configuration.getBoolean("elasticsearch.embedded").getOrElse(true)

  lazy val ELASTIC_URL =
    Play.application.configuration.getString("elasticsearch.url").getOrElse("http://localhost:9200")

  lazy val INDEX_NAME =
    Play.application.configuration.getString("elasticsearch.index").getOrElse("xamplez")

  lazy val TYPE_NAME =
    Play.application.configuration.getString("elasticsearch.type").getOrElse("gists")

  lazy val CLUSTER_NAME =
    Play.application.configuration.getString("elasticsearch.clustername").getOrElse("xamplez")

  lazy val INDEX_URL = s"$ELASTIC_URL/$INDEX_NAME"
  lazy val INSERT_URL = s"$ELASTIC_URL/$INDEX_NAME/$TYPE_NAME"
  lazy val SEARCH_URL = INDEX_URL + "/_search"

  private var node: Option[Node] = None

  private def getIndex(name: String): Future[Either[String, JsObject]] = {
    WS.url(s"$ELASTIC_URL/$name/_settings")
      .get()
      .map { r =>
        if(r.status == 200) Right(r.json.as[JsObject])
        else Left(s"status:${r.status} statusText:${r.statusText} error:${r.body}")
      }

  }

  private def createIndex(name: String): Future[Either[String, JsObject]] = {
    WS.url(s"$ELASTIC_URL/$name")
      .put(play.api.mvc.Results.EmptyContent())
      .map { r =>
        if(r.status == 200) Right(Json.obj("res" -> "ok"))
        else Left(s"status:${r.status} statusText:${r.statusText} error:${r.body}")
      }
  }

  def start {

    import org.elasticsearch.common.settings._
    import org.elasticsearch.common.io.stream._
    import java.io._

    if(IS_EMBEDDED) {
      play.Logger.info("Starting Local ES")

      import org.elasticsearch.common.settings.loader.SettingsLoader

      val settings = Play.resourceAsStream("elasticsearch.yaml").map{ s =>
        ImmutableSettings.settingsBuilder().loadFromStream("elasticsearch.yaml", s).build
      }

      val n = nodeBuilder().clusterName(CLUSTER_NAME).local(true)

      node = Some(settings.map(n.settings _).getOrElse(n).node)
    } else {
      play.Logger.info(s"Creating index: $INDEX_NAME")
      val resp = Await.result(
        getIndex(INDEX_NAME).flatMap{ 
          case Left(error) => 
            if(error.contains("IndexMissingException")) createIndex(INDEX_NAME)
            else throw new RuntimeException("Could create index: "+error)
          case Right(settings) => play.Logger.info(s"Index already existing: $settings"); Future.successful()
        },
        Duration("10 seconds")
      )

    }
  }

  def stop() {
    if(IS_EMBEDDED) {
      play.Logger.info("Stopping ES")
      for(n <- node) n.stop
    }
  }

  private def buildSearch(query: String) =
    Json.obj("query" -> Json.obj(
      "query_string" -> Json.obj(
        "fields" -> Seq("description", "tags^10"),
        "query"  -> query
      )
    ))

  val descIdReader = (
    (__ \ "description").read[String] and
    (__ \ "id").read[String]
  ).tupled

  def insert(json: JsObject): Future[Either[Response, JsValue]] = {
    descIdReader.reads(json).map { case (desc, id) =>
      val withTags = json ++ Json.obj("tags" -> Tag.fetchTags(desc))
      play.Logger.debug(s"Search : inserting $withTags")
      WS.url(s"$INSERT_URL/$id")
        .put(withTags)
        .map { r =>
          if(r.status == 200 || r.status == 201) Right(r.json)
          else Left(r)
        }
    } recoverTotal { e => Future(Right(Json.obj())) }
  }

  def search(q: String, pretty: Boolean = true): Future[Either[Response, JsValue]] = {
    play.Logger.debug(s"Search : query $q")
    search(buildSearch(q), pretty)
  }

  private def search(query: JsObject, pretty: Boolean): Future[Either[Response, JsValue]] =
    WS.url(SEARCH_URL)
      .post(query)
      .map { r =>
        if(r.status == 200) Right(r.json)
        else Left(r)
      }

  val queryTags = Json.obj(
    "query" -> Json.obj("match_all" -> Json.obj()),
    "size"  -> 0,
    "facets" -> Json.obj(
      "tags" -> Json.obj("terms" -> Json.obj("field" -> "tags"))
    )
  )

  def tags = search(queryTags, true)

  val queryLastCreated = Json.obj(
    "query" -> Json.obj( "match_all" -> Json.obj() ),
    "size" -> 1,
    "sort" -> Json.arr(
      Json.obj(
        "created_at" -> "desc"
      )
    )
  )

  def lastCreated = search(queryLastCreated, true)

  val queryLastUpdated = Json.obj(
    "query" -> Json.obj( "match_all" -> Json.obj() ),
    "size" -> 1,
    "sort" -> Json.arr( Json.obj(
      "updated_at" -> "desc"
    ))
  )

  def lastUpdated = search(queryLastUpdated, true)

}

object Search extends ElasticSearch