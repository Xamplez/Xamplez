package controllers

import play.api._
import play.api.mvc._
import services.github._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.Play.current

object Application extends GithubOAuthController {

  val ROOT_GIST = Play.application.configuration.getString("gist.root").get
  val ROOT_GIST_URL = s"https://gist.github.com/$ROOT_GIST"

  def main(any: String) = Action {
    Ok(views.html.main(ROOT_GIST_URL))
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
