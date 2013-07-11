package controllers

import play.api._
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.Play.current

import java.net.URLConnection
import scala.collection._
import scala.concurrent.Future

import services.github.GithubWS._

object GistAssets extends Controller{
  lazy val GIST_CONF = Play.application.configuration.getLong("config.external.gist")

  val synchroSet = new mutable.HashSet[String] with mutable.SynchronizedSet[String]

  def invalidate = Action{
    val values = synchroSet.toSeq
    synchroSet --= values
    values.foreach { v => Cache.remove(v) }
    Ok("Done")
  }

  def at( fileName: String ) = Action{
    Async{
      (( Cache.getAs[String](fileName), GIST_CONF ) match {
        case ( None, Some(id) ) => Gist.getFile(id, fileName).map{ 
          case Some(file) => {
            synchroSet.add(fileName)
            Cache.set(fileName, file)
            Some(file)
          }
          case f => f
        }
        case ( file, _ ) => Future(file)
        case _ => Future(None)
      }).map{ 
        case Some(str) => {
          val guess = Option(URLConnection.guessContentTypeFromName(fileName))
          val ext = fileName.substring(fileName.lastIndexOf(".") + 1)
          val content = (guess, ext) match {
            case ( None, "css" ) => "text/css"
            case ( None, "js" ) => "Application/javascript"
            case _ => "text/plain"
          }

          Ok(str).withHeaders( "Content-Type" -> s"$content; charset=utf-8" )
        }
        case None => NotFound
      }
    }
  }
}