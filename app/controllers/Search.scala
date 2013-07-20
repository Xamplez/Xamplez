package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import services.search.{ GistSearch => S }

import concurrent.Future

object Search extends Controller {

  def search(q: String, sorts: Option[String] = None, from: Option[Int] = None, size: Option[Int] = None) = Action {
    Async{
      val s = sorts.map(_.split(",").map(_.splitAt(1)).collect {
                case ("+", field) => Json.obj(field -> "asc")
                case ("-", field) => Json.obj(field -> "desc")
              }.reduceLeft(_ ++ _))
      S.search(q, s, from, size).map(
        _.fold(
          resp => InternalServerError(resp.body),
          json => Ok(json))
      )
    }
  }

  def insert = Action(parse.json) { request =>
    val jsObject = request.body.as[JsObject]
    Async{
      S.insert(jsObject).map(
        _.fold(
          err => InternalServerError(err),
          r => Ok(r)
        )
      )
    }
  }

  def tags = Action { request =>
    Async {
      S.tags.map(
        _.fold(
          err => InternalServerError(err.json \ "error"),
          r => Ok(r))
      )
    }
  }

  def lastCreated = Action { request =>
    Async {
      S.lastCreated.map(
        _.fold(
          err => InternalServerError(err.json \ "error"),
          r => Ok(r))
      )
    }
  }
}