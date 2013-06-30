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
    Ok(views.html.main())
  }

  def notFound(any: String) = Action {
    NotFound("404")
  }

  def index = Action {
    Ok(views.html.index())
  }

  def gist = Action {
    Ok(views.html.gist())
  }

  def stats = Action {
    Ok(views.html.stats())
  }

  def about = Action {
    Ok(views.html.about())
  }

}
