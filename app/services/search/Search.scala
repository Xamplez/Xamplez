package services.search

import scala.concurrent.Future

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.ws._

import play.api.libs.concurrent.Execution.Implicits._

import models._

trait ElasticSearch {

  import org.elasticsearch.node._
  import org.elasticsearch.node.NodeBuilder._

  val ELASTIC_URL = "http://localhost:9200"
  val INDEX_URL = ELASTIC_URL + "/examples/gists"
  val SEARCH_URL = INDEX_URL + "/_search"

  val CLUSTER_NAME = "play_by_example"

  private var node: Option[Node] = None

  def start(implicit app: Application) {

    import org.elasticsearch.common.settings._
    import org.elasticsearch.common.io.stream._
    import java.io._

    play.Logger.info("Starting ES")

    import org.elasticsearch.common.settings.loader.SettingsLoader

    val settings = Play.resourceAsStream("elasticsearch.yaml").map{ s =>
      ImmutableSettings.settingsBuilder().loadFromStream("elasticsearch.yaml", s).build
    }

    val n = nodeBuilder().clusterName(CLUSTER_NAME).local(true)

    node = Some(settings.map(n.settings _).getOrElse(n).node)
  }

  def stop() {
    play.Logger.info("Stopping ES")
    for(n <- node) n.stop
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
      WS.url(s"$INDEX_URL/$id")
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