package controllers

import org.joda.time.DateTime

import play.api._
import play.api.mvc._
import play.api.Play.current

import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {
  lazy val GIST_ROOTS = Play.application.configuration.getLongList("gist.roots")
  lazy val ROOT_GIST_URL = "https://gist.github.com/%s".format( GIST_ROOTS.get.get(0) )

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
