package services.search

import scala.concurrent.Future

import play.api._
import play.api.libs.json._
import play.api.libs.ws._

import play.api.libs.concurrent.Execution.Implicits._

import models._

trait ElasticSearch {

  import org.elasticsearch.node._
  import org.elasticsearch.node.NodeBuilder._

  val ELASTIC_URL = "http://localhost:9200"
  val INDEX_URL = ELASTIC_URL + "/examples/gists"
  val SEARCH_URL = INDEX_URL + "/_search"

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

    val n = nodeBuilder()
      .clusterName("play_by_example")
      .local(true)

    node = Some(settings.map(n.settings _).getOrElse(n).node)
  }

  def stop() {
    play.Logger.info("Stopping ES")
    for(n <- node) n.stop
  }

  private def buildSearch(query: String) =
    Json.obj("query" ->
      Json.obj(
      "query_string" -> Json.obj(
          "fields" -> Seq("description", "tags^10"),
          "query"  -> query)))

  def insert(json: JsObject): Future[Either[Response, JsValue]] = {
    (json  \ "description").validate[String] map { desc =>
      val withTags = json ++ Json.obj("tags" -> Tag.fetchTags(desc))
      WS.url(INDEX_URL)
        .post(withTags)      
        .map { r =>
          if(r.status == 200 || r.status == 201) Right(r.json)
          else Left(r)
        }
    } recoverTotal { e => Future(Right(Json.obj())) }

  }

  def search(s: String, pretty: Boolean = true): Future[Either[Response, JsValue]] =
    WS.url(SEARCH_URL)
      .post(buildSearch(s))
      .map { r =>
        if(r.status == 200) Right(r.json)
        else Left(r)
      }

  def tags() = {
    val q = Json.obj(
      "query" -> Json.obj("match_all" -> Json.obj()),
      "size"  -> 0,
      "facets" -> Json.obj(
        "tags" -> Json.obj("terms" -> Json.obj("field" -> "tags"))))

    WS.url(SEARCH_URL)
      .post(q)
      .map { r =>
        if(r.status == 200) Right(r.json)
        else Left(r)
      }
  }
}

object Search extends ElasticSearch