package controllers

import play.api._
import play.api.mvc._
import services.github._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.Play.current

object Application extends GithubOAuthController {

  val originalGistUrl = "https://gist.github.com/"+Play.application.configuration.getString("gist.root").get

  def main(any: String) = Action {
    Ok(views.html.main(originalGistUrl))
  }

  def notFound(any: String) = Action {
    NotFound("404")
  }

  def index = Action {
    Ok(views.html.index())
  }

  def search = Action {
    Ok(views.html.search())
  }

}
