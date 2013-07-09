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
  val tweetUpdateUrl = "https://api.twitter.com/1.1/statuses/update.json"
  val tweetSearchUrl = "https://api.twitter.com/1.1/search/tweets.json"

  lazy val account = Play.application.configuration.getString("twitter.account").getOrElse("")

  lazy val consumerKey = ConsumerKey(
  	Play.application.configuration.getString("twitter.consumer.key").getOrElse(""),
  	Play.application.configuration.getString("twitter.consumer.secret").getOrElse("")
  )

  lazy val accessToken = RequestToken(
  	Play.application.configuration.getString("twitter.token.key").getOrElse(""),
  	Play.application.configuration.getString("twitter.token.secret").getOrElse("")
  )

  def tweet( msg: String ) = WS.url(tweetUpdateUrl + "?status=%s".format(URLEncoder.encode(msg, "UTF-8")))
    .sign(OAuthCalculator(consumerKey, accessToken))
    .post("ignored")

  def search( search: String ) = WS.url(tweetSearchUrl + "?q=%s".format(URLEncoder.encode(search, "UTF-8")))
    .sign(OAuthCalculator(consumerKey, accessToken))
    .get()

  def isTweeted( id: Long ): Future[Boolean] = search(s"$id from:$account").map { response =>
  	( response.json \ "statuses" ).as[Seq[JsValue]].nonEmpty
  }
}