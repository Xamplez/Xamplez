package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {
  lazy val GIST_ROOT = Play.application.configuration.getLong("gist.root")
  lazy val ROOT_GIST_URL = "https://gist.github.com/%s".format( GIST_ROOT.map(_.toString).getOrElse("Missing") )

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
