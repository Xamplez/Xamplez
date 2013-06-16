package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import services.search.{ Search => S }

import concurrent.Future

object Search extends Controller {

  def search(q: String) = Action {
    Async{
      S.search(q).map(
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
          err => InternalServerError(err.json \ "error"),
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