package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._

object Gists extends Controller {

	// TODO: return the JSON of the corresponding gist
	def findById(id: String) = Action {
		Ok(Json.obj())
	}

}