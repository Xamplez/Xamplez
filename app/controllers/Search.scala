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

  def search(q: String) = Action { implicit req =>

     val result = S.search(q)
     Async(result.map{ r =>
       r.fold(
         resp => InternalServerError(resp.body),
         json => Ok(json))
     })
   }


   def insert = Action(parse.json) { implicit request =>
    val jsObject = request.body.as[JsObject]
      val r = S.insert(jsObject).map(
       _.fold(
         err => InternalServerError(err.json \ "error"),
         r => Ok(r))
      )

    Async(r)
   }


   def tags = Action { implicit request =>
     val r = S.tags().map(
       _.fold(
         err => InternalServerError(err.json \ "error"),
         r => Ok(r)))

     Async(r)
   }

}