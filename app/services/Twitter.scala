package services

import concurrent.Future
import java.net.URLEncoder

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.oauth._
import play.api.libs.ws._
import play.api._
import play.api.Play.current

object Twitter{
  val log = play.api.Logger("application.services.twitter")
  val tweetUpdateUrl = "https://api.twitter.com/1.1/statuses/update.json"

  val access = {
    val config = Play.application.configuration
    (
      config.getString("twitter.consumer.key"),
      config.getString("twitter.consumer.secret"),
      config.getString("twitter.token.key"),
      config.getString("twitter.token.secret")
    ) match {
      case ( Some(ck), Some(cs), Some(tk), Some(ts) ) => Some(( ConsumerKey(ck, cs), RequestToken(tk, ts) ))
      case _ => log.warn("Missing twitter configurations keys."); None
    }
  }

  val ( tweetableDelay, tweetableStars, tags, rootUrl ) = {
    val config = Play.application.configuration
    (
      config.getInt("twitter.tweetable.delay").getOrElse(1),
      config.getInt("twitter.tweetable.stars").getOrElse(1),
      config.getString("twitter.tags").getOrElse("#xamplez"),
      config.getString("application.root.url").getOrElse("https://gist.github.com")
    )
  }

  def tweet( msg: String ): Future[Boolean] = access.map { a =>
    WS.url(tweetUpdateUrl + "?status=%s".format(URLEncoder.encode(msg, "UTF-8")))
      .sign(OAuthCalculator(a._1, a._2))
      .post("ignored")
      .map{ r =>
        r.status match {
          case 200 => true
          case 403 if ( r.json \ "errors" \\ "code" ).map(_.as[Int]).contains(187) => true
          case _ => log.debug("Tweet failed : %s - %s".format(r.status, r.body)); false
        }
      }
  }.getOrElse( Future(false) )

  private def clean( str: String, max: Int ) = 
    if( str.length > max ){ str.substring(0, max - 3) + "..." } else { str }

  def tweet( js: JsValue ): Future[Boolean] = {
    val id = ( js \ "id" ).as[String].toLong
    val description = ( js \ "description" ).as[String] 
    val length = tags.length + 1 + ( if( rootUrl.startsWith("https") ) 23 else 22 ) + 1

    tweet( "%s/%s %s %s".format(rootUrl, id, tags, clean(description, 140 - length)) )
  }
}
