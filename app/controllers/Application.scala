package controllers

import play.api._
import play.api.mvc._
import services.github._
import play.api.libs.concurrent.Execution.Implicits._

object Application extends GithubOAuthController {
  def main(any: String) = Action {
    Ok(views.html.main())
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
