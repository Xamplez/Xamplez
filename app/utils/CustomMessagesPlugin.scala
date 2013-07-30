package play.api.i18n

import play.api._
import play.api.Application
import play.api.i18n._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._

import scalax.file._
import scalax.io.JavaConverters._
import scalax.io.Resource

/**
 * Play Plugin for internationalisation.
 */
class CustomMessagesPlugin(app: Application) extends MessagesPlugin(app) {
  lazy val gistId = Play.application.configuration.getLong("config.external.gist")

  def loadMessages(file: String): Map[String, String] = {
    app.classloader.getResources(file).asScala.toList.reverse.map { messageFile =>
      new Messages.MessagesParser(messageFile.asInput, messageFile.toString).parse.map { message =>
        message.key -> message.pattern
      }.toMap
    }.foldLeft(Map.empty[String, String]) { _ ++ _ }
  }

  def loadGistMessages( fileName: String ): Future[Map[String, String]] = {
    gistId.map{ id =>
      services.github.GithubWS.Gist.getFileUrl(id, fileName).map{
        case Some(url) => {
          val input = Resource.fromURL(url)
          new Messages.MessagesParser(input, url).parse.map{ message =>
            message.key -> message.pattern
          }.toMap
        }
        case None => Map.empty[String, String]
      }.recover{
        case t:Throwable => Map.empty[String, String]
      }
    }.getOrElse( Future(Map.empty) )
  }

  def messages = {
    Future.sequence(
      ( Lang.availables(app)
        .map( l => ( l.code, "message.%s".format(l.code)) ) :+ ( ("default", "messages") ) )
        .map { files =>
          loadGistMessages(files._2).map{ values =>
            (files._1, loadMessages(files._2) ++ values)
          }
        }
    ).map( v => MessagesApi(v.toMap) )
  }

  /**
   * The underlying internationalisation API.
   */
  override lazy val api = Await.result( messages, 60 seconds )

  /**
   * Loads all configuration and message files defined in the classpath.
   */
  override def onStart() = api

}