package services.search

import scala.concurrent.Future

import play.api.libs.json._
import play.api.libs.ws._

import models._

trait ElasticSearch {

  import org.elasticsearch.node._
  import org.elasticsearch.node.NodeBuilder._

  import play.api.libs.concurrent.Execution.Implicits._

  val ELASTIC_URL = "http://localhost:9200"
  val INDEX_URL = ELASTIC_URL + "/examples/gists"
  val SEARCH_URL = INDEX_URL + "/_search"

  private var node: Option[Node] = None

  def start() {
    play.Logger.info("Starting ES")
    val n = nodeBuilder()
      .clusterName("play_by_example")
      .local(true)
      .node()
    node = Some(n)
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

  def insert(json: JsObject) = {
    val desc = (json  \ "description").as[String]
    val withTags = json ++ Json.obj("tags" -> Tag.fetchTags(desc))
    WS.url(INDEX_URL).post(withTags)
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
      "size" -> 0,
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