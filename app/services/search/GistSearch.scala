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

trait GistSearch extends EsAPI{

  import org.elasticsearch.node._
  import org.elasticsearch.node.NodeBuilder._

  lazy val TYPE_NAME =
    Play.application.configuration.getString("elasticsearch.type").getOrElse("gists")

  lazy val SEARCH_URL = INDEX_URL + "/_search"
  lazy val COUNT_URL = INDEX_URL + "/_count"
  lazy val UPDATE_URL = s"$elasticUrl/$indexName/$TYPE_NAME"

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
    }.distinct.foldLeft(Seq[String](), Seq[String]()){
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

        insert(TYPE_NAME, id, fullJson)
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

  def twitted( id: Long ): Future[Either[String, JsValue]] = {
    val upd = Json.obj( "script" -> "ctx._source.twitted = true" )
    play.Logger.debug("going to update "+upd)

    WS.url(s"$UPDATE_URL/$id/_update")
      .post(upd)
      .map { r =>
        if(r.status == 200 || r.status == 201) Right(r.json)
        else Left(s"Couldn't update $id (status:${r.status} msg:${r.statusText}")
      }
  }

  def delete(id: Long): Future[Response] = delete(TYPE_NAME, id.toString)

  val typeFilter = Json.obj(
    "type" -> Json.obj(
      "value" -> TYPE_NAME
    )
  )

  private def filtered(query: JsObject, filter: JsObject): JsObject = {
    Json.obj(
      "filtered" -> Json.obj(
        "query" -> query,
        "filter" -> filter
      )
    )
  }

  private def buildSearch(query: String, sorts: Option[JsObject] = Some(Json.obj("stars" -> "desc")), from: Option[Int] = None, size: Option[Int] = None): JsObject = {
    val q =
      if(query.isEmpty) Json.obj("match_all" -> Json.obj())
      else Json.obj(
             "query_string" -> Json.obj(
               "fields" -> Seq("tags^10", "author_login^10", "description"),
               "default_operator" -> "AND",
               "query"  -> query
             )
           )

    val search = 
      Json.obj("query" -> filtered(q, typeFilter)) ++
      sorts.map( s => Json.obj("sort" -> Json.arr(s))).getOrElse(Json.obj("sort" -> Json.arr(Json.obj("stars" -> "desc")))) ++
      from.map(from => Json.obj("from" -> from)).getOrElse(Json.obj()) ++
      size.map(from => Json.obj("size" -> size)).getOrElse(Json.obj())

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
    "query"  -> filtered(Json.obj("match_all" -> Json.obj()), typeFilter),
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

  val queryAuthors = Json.obj(
    "query"  -> filtered(Json.obj("match_all" -> Json.obj()), typeFilter),
    "size"   -> 1000,
    "facets" -> Json.obj(
      "authors" -> Json.obj(
        "terms" -> Json.obj(
          "field" -> "author_login",
          "size" -> 1000
        )
      )
    )
  )

  def authors = search(queryAuthors, true)

  val queryLastCreated = Json.obj(
    "query" -> filtered(Json.obj( "match_all" -> Json.obj() ), typeFilter),
    "size" -> 100,
    "sort" -> Json.arr(
      Json.obj(
        "created_at" -> "desc"
      )
    )
  )

  def lastCreated = search(queryLastCreated, true)

  val queryLastUpdated = Json.obj(
    "query" -> filtered(Json.obj( "match_all" -> Json.obj() ), typeFilter),
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
      case Right(js) => (js \ "hits" \ "hits" \\ "_source").headOption
    }
  }

  def queryByIds(ids: Seq[Long]) = Json.obj(
    "query" -> Json.obj( "ids" -> Json.obj( "values" -> Json.arr(ids)) ),
    "size" -> ids.length
  )
  def byIds(ids: Seq[Long]): Future[Either[String, Seq[JsValue]]] = {
    search(queryByIds(ids), true).map{
      case Left(r) => Left(s"Couldn't search $ids (status:${r.status} msg:${r.statusText}")
      case Right(js) => Right( (js \ "hits" \ "hits" \\ "_source") )
    }
  }

  def queryTwittable( minCreated: DateTime, maxUpdated: DateTime, minStars: Int ) = Json.obj(
    "filter" -> Json.obj( "and" -> Json.arr(
      Json.obj( "missing" -> Json.obj("field" -> "twitted") ),
      Json.obj( "range" -> Json.obj( "created_at" -> Json.obj( "ge" -> minCreated.toString() ) ) ),
      Json.obj( "or" -> Json.arr(
        Json.obj( "range" -> Json.obj( "updated_at" -> Json.obj( "lt" -> maxUpdated.toString() ) ) ),
        Json.obj( "range" -> Json.obj( "stars" -> Json.obj( "ge" -> minStars ) ) )
      ))
    ))
  )
  def twittable( minCreated: DateTime, maxUpdated: DateTime, minStars: Int): Future[Set[Long]] =
    search(queryTwittable(minCreated, maxUpdated, minStars), true).map {
      case Left(r) => play.Logger.debug("Couldn't find any twittable Gists"); Set.empty
      case Right(js) => (js \ "hits" \ "hits" \\ "_id").map( _.as[String].toLong ).toSet
    }

  def count: Future[Long] = {
    WS.url(s"$COUNT_URL?q=_type:gists")
      .get()
      .flatMap { r =>
        if(r.status == 200) 
          Future.successful((r.json \ "count").as[Long])
        else Future.failed(
          new RuntimeException(s"couldn't count gists ${r.status} ${r.statusText}" )
        )
      }
  }
}

object GistSearch extends GistSearch
