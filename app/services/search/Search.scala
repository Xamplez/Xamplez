package services.search

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
  lazy val UPDATE_URL = s"$ELASTIC_URL/$INDEX_NAME/$TYPE_NAME"

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

  val descIdFilesReader = (
    (__ \ "description").readNullable[String] and
    (__ \ "id").read[String] and
    (__ \ "files").read[Seq[String]]
  ).tupled

  val descIdFilesStarsReader = (
    (__ \ "description").readNullable[String] and
    (__ \ "id").read[String] and
    (__ \ "files").read[Seq[String]] and
    (__ \ "stars").read[Int]
  ).tupled

  // Magical string interpolation to pattern match regex
  implicit class Regex(sc: StringContext) {
    def r = new scala.util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  val ext2Lang = Map(
    "scala" -> ("scala", false),
    "java"  -> ("java", false),
    "py"    -> ("python", true),
    "js"    -> ("javascript", true),
    "json"  -> ("json", false),
    "html"  -> ("html", false),
    "htm"   -> ("html", false),
    "png"   -> ("png", false),
    "jpg"   -> ("jpg", false),
    "TXT"   -> ("text", false),
    "text"  -> ("text", false),
    "less"  -> ("less", false),
    "css"   -> ("css", false)
  )

  private def getLangs(files: Seq[String]): (Seq[String], Seq[String]) = {
    files.filter{
      case r"\w*LICENSE.txt" => false
      case r"\w*\.md$$" => false
      case r"\w*\.txt$$" => false
      case r"\w*\.[^\.]+$$" => true
      case _ => false
    }.map{ file =>
      file.split("\\.(?=[^\\.]+$)")(1)
    }.foldLeft(Seq[String](), Seq[String]()){
      case ((langs, tagLangs), file) =>
        ext2Lang.get(file) match {
          case None => 
            play.Logger.warn(s"langage $file not recognized... not considering as language but indexing as tag")
            (langs, tagLangs :+ file)
          case Some((lg, keep)) =>
            (langs :+ lg, if(keep) (tagLangs :+ file :+ lg) else (tagLangs :+ file))
        }
        
    }
  }

  def insert(json: JsObject): Future[Either[String, JsValue]] = {
    descIdFilesReader.reads(json).map{
      case (Some(desc), id, files) if desc.length>0 =>
        val tags = Tag.fetchTags(desc)
        val (langs, tagLangs) = getLangs(files)
        val fullJson =
          json.delete(__ \ "files").as[JsObject] ++
          Json.obj(
            "langs" -> langs,
            "tags" -> (tags.map(_.name) ++ tagLangs)
          )

        play.Logger.debug(s"Search : inserting $fullJson")
        WS.url(s"$INSERT_URL/$id")
          .put(fullJson)
          .map { r =>
            if(r.status == 200 || r.status == 201) Right(r.json)
            else Left(s"Couldn't insert $id (status:${r.status} msg:${r.statusText}")
          }
      case (_, id, _) =>
        play.Logger.warn(s"Can't insert $json because description is null or empty")
        Future(Left(s"Can't insert $json because description is null or empty"))
    }.recoverTotal{ e => Future(Left(s"Can't insert due to json error ${e}")) }
  }

  def updateStars(id: Long, stars: Int): Future[Response] = {
    val obj = Json.obj(
      "script" -> "ctx._source.stars = stars",
      "params" -> Json.obj(
        "stars" -> stars
      )
    )
    WS.url(s"$UPDATE_URL/$id/_update").post(obj)
  }

  def update(id: Long, json: JsObject, stars: JsObject): Future[Either[String, JsValue]] = {
    descIdFilesStarsReader.reads(json ++ stars).map{
      case (Some(desc), _, files, stars) if desc.length>0 =>
        byId(id).flatMap{ _ match {
          case Some(kjson) =>
        
            val kstars = (kjson \ "stars").as[Int]
            val kdesc  = (kjson \ "description").as[String]
            val klangs = (kjson \ "langs").as[Seq[String]]
            val ktags = (kjson \ "tags").as[Seq[String]]
            val tags = Tag.fetchTags(desc).map(_.name)

            val (langs, tagLangs) = getLangs(files)

            var script = ""
            var obj = Json.obj()

            if(kdesc != desc){
              script += "ctx._source.description = description;"
              obj = obj ++ Json.obj("description" -> desc)
            }
            if(ktags != tags ++ tagLangs){
              script += "ctx._source.tags = tags;"
              obj = obj ++ Json.obj("tags" -> (tags ++ tagLangs))
            }
            if(klangs != langs){
              script += "ctx._source.langs = langs;"
              obj = obj ++ Json.obj("langs" -> langs)
            }
            if(kstars != stars){
              script += "ctx._source.stars = stars;"
              obj = obj ++ Json.obj("stars" -> stars)
            }

            if(script.isEmpty) Future.successful(Left(s"$id not modified"))
            else {
              val upd = Json.obj(
                "script" -> script,
                "params" -> obj
              )
              play.Logger.debug("going to update "+upd)
              val a: Future[Either[String, JsValue]] = WS.url(s"$UPDATE_URL/$id/_update")
                .post(upd)
                .map { r =>
                  if(r.status == 200 || r.status == 201) Right(r.json)
                  else Left(s"Couldn't update $id (status:${r.status} msg:${r.statusText}")
                }
              a
            }

          case None  => Future.successful(Left(s"$id not found"))
        } }

      case (_, id, _, _) =>
        play.Logger.warn(s"Can't update $json because description is null or empty")
        Future(Left(s"Can't update $json because description is null or empty"))
    }.recoverTotal{ e => Future.successful(Left(s"Can't update due to json error ${e}")) }
  }

  def delete(id: Long): Future[Response] = {
    play.Logger.debug(s"Search : deleting $id")
    WS.url(s"$INSERT_URL/$id").delete
  }

  private def buildSearch(query: String, sorts: Option[JsObject] = Some(Json.obj("stars" -> "desc")), from: Option[Int] = None, size: Option[Int] = None) = {
    val q = 
      if(query.isEmpty) Json.obj("match_all" -> Json.obj())
      else Json.obj(
             "query_string" -> Json.obj(
               "fields" -> Seq("description", "tags^10"),
               "default_operator" -> "AND",
               "query"  -> query
             )
           )
    val obj = Json.obj(
      "query" -> q
    )

    val search = obj ++
    sorts.map( s => Json.obj("sort" -> Json.arr(s))).getOrElse(Json.obj("sort" -> Json.arr(Json.obj("stars" -> "desc")))) ++
    from.map(from => Json.obj("from" -> from)).getOrElse(Json.obj()) ++
    size.map(from => Json.obj("size" -> size)).getOrElse(Json.obj())
    play.Logger.debug("query:"+search)
    search
  }

  private def search(query: JsObject, pretty: Boolean): Future[Either[Response, JsValue]] =
    WS.url(SEARCH_URL)
      .post(query)
      .map { r =>
        if(r.status == 200) Right(r.json)
        else Left(r)
      }

  def search(q: String, sorts: Option[JsObject] = Some(Json.obj("stars" -> "desc")), from: Option[Int] = None, size: Option[Int] = None, pretty: Boolean = true): Future[Either[Response, JsValue]] = {
    play.Logger.debug(s"Search : query=$q sorts=$sorts from=$from size=$size")
    search(buildSearch(q, sorts, from, size), pretty)
  }

  val queryTags = Json.obj(
    "query"  -> Json.obj("match_all" -> Json.obj()),
    "size"   -> 1000,
    "facets" -> Json.obj(
      "tags" -> Json.obj(
        "terms" -> Json.obj(
          "field" -> "tags",
          "size" -> 1000
        )
      )
    )
  )

  def tags = search(queryTags, true)

  val queryLastCreated = Json.obj(
    "query" -> Json.obj( "match_all" -> Json.obj() ),
    "size" -> 100,
    "sort" -> Json.arr(
      Json.obj(
        "created_at" -> "desc"
      )
    )
  )

  def lastCreated = search(queryLastCreated, true)

  val queryLastUpdated = Json.obj(
    "query" -> Json.obj( "match_all" -> Json.obj() ),
    "size" -> 100,
    "sort" -> Json.arr( Json.obj(
      "updated_at" -> "desc"
    ))
  )

  def lastUpdated = search(queryLastUpdated, true)

  def queryById(id: Long) = Json.obj(
    "query" -> Json.obj( "ids" -> Json.obj( "values" -> Json.arr(id.toString)) ),
    "size" -> 1
  )
  def byId(id: Long): Future[Option[JsValue]] = {
    search(queryById(id), true).map{
      case Left(r) => throw new RuntimeException(s"Couldn't search $id (status:${r.status} msg:${r.statusText}")
      case Right(js) =>
        val hits = (js \ "hits").as[JsObject]
        val nb = (hits \ "total").as[Int]
        if(nb >= 1) Some((( (hits \ "hits").as[JsArray] ).apply(0) \ "_source").as[JsObject])
        else None
    }
  }

  def queryByIds(ids: Seq[Long]) = Json.obj(
    "query" -> Json.obj( "ids" -> Json.obj( "values" -> Json.arr(ids)) ),
    "size" -> ids.length
  )
  def byIds(ids: Seq[Long]): Future[Either[String, Seq[JsValue]]] = {
    search(queryByIds(ids), true).map{
      case Left(r) => Left(s"Couldn't search $ids (status:${r.status} msg:${r.statusText}")
      case Right(js) => 
        val hits = (js \ "hits").as[JsObject]
        val nb = (hits \ "total").as[Int]
        if(nb >= 1) Right((hits \ "hits" \ "_source").as[Seq[JsObject]])
        else Right(Seq())
    }
  }

}

object Search extends ElasticSearch
