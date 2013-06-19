package services.github

import services.auth.OAuth2Token

import play.api._
import play.api.libs.ws._
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import play.api.Play.current
import scala.util.matching.Regex

import concurrent.Future

object GithubWS {
  lazy val clientId = Play.application.configuration.getString("github.clientId")
  lazy val clientSecret = Play.application.configuration.getString("github.clientSecret")


  def fetch(url: String, accept: String = "application/json"): WSRequestHolder = {
    val ws = WS.url("https://api.github.com" + url).withHeaders("Accept" -> accept)
    (clientId, clientSecret) match {
      case (Some(id),Some(secret)) => {
          ws.withQueryString("client_id" -> id)
            .withQueryString("client_secret" -> secret)
        }
      case _ => ws
    }
  }

  def fetchWithToken(url: String, accept: String = "application/json")(implicit token: OAuth2Token): WSRequestHolder = {
    WS.url("https://api.github.com" + url)
      .withQueryString("access_token" -> token.accessToken)
      .withHeaders("Accept" -> accept)
  }

  object User {

    /**
     * Return information about the connected user
     */
    def me(implicit token: OAuth2Token) = {
      fetch("/users/user").get.map(_.json)
    }

    /**
     * Return information about an user
     */
    def info(user: String)(implicit token: OAuth2Token) = {
      fetch(s"/users/$user").get.map(_.json)
    }
  }

  object Gist {
    private val cleanJson = (
      (__ \ "id").json.pickBranch and
      (__ \ "description").json.pickBranch and
      (__ \ "created_at").json.pickBranch and
      (__ \ "updated_at").json.pickBranch and
      (__ \ "author_login").json.copyFrom( (__ \ "user" \ "login").json.pick )
    ).reduce

    private val cleanJsonWithFiles = (
      (__ \ "id").json.pickBranch and
      (__ \ "description").json.pickBranch and
      (__ \ "created_at").json.pickBranch and
      (__ \ "updated_at").json.pickBranch and
      (__ \ "author_login").json.copyFrom( (__ \ "user" \ "login").json.pick ) and
      (__ \ "files").json.copyFrom(
        (__ \ "files").json.pick[JsObject].map{ obj => JsArray(obj.fields.map(_._1).map(JsString(_))) }
      )
    ).reduce

    /**
     * Create a new Gist with 2 files: the question and an empty answer
     * @return The Gist ID if success, otherwise None
     */
    def create(question: String, extension: String, author: String): Future[Option[Long]] = {
      val data = Json.obj(
        "description" -> s"PlayByExample: $question",
        "public" -> true,
        "files" -> Map(
          s"0. $question.txt" -> Map(
            "content" -> question
          ),
          s"1. Answer.$extension" -> Map(
            "content" -> "// Implement your solution here"
          )
        )
      )

      fetch("/gists").post(data).map(result =>
        result.status match {
          case 201 => (result.json \ "id").asOpt[String].map(_.toLong)
          case _ => None
        })
    }

    // Hack Github API - retrieve the stars number from the gist page!
    def stars(gistId: Long): Future[Option[Long]] = {
      val starsHtmlPrefix = "Stars\n            <span class=\"counter\">"
      WS.url(s"https://gist.github.com/$gistId/stars").get.map { r =>
        r.status match {
          case 200 =>
            r.body.indexOf(starsHtmlPrefix) match {
              case index if index != -1 =>
                val str = r.body.drop(index+starsHtmlPrefix.size).takeWhile(_.isDigit)
                if (str.size == 0) None else Some(str.toInt)
              case _ => None
            }
          case _ => None
        }
      }
    }

    def star(gistId: Long) = {
      fetch(s"/gists/$gistId/star").put("")
    }

    def unstar(gistId: Long) = {
      fetch(s"/gists/$gistId/star").delete()
    }

    def get(gistId: Long): Future[JsValue] = {
      fetch(s"/gists/$gistId").get.map(_.json)
    }

    def forksId(gistId: Long): Future[Seq[Long]] = {
      get(gistId).map{ json =>
        (json \ "forks").as[JsArray].value.map{ fork =>
          (fork \ "id").as[String].toLong
        }
      }
    }

    def listForks(gistId: Long): Future[Seq[JsObject]] = {
      forksId(gistId).flatMap{ ids =>
        Future.sequence(ids.map{ id =>
          get(id).map{ fork =>
            fork.transform(cleanJsonWithFiles).getOrElse(JsNull).as[JsObject]
          }
        })
      }
    }

    def listForksOld(gistId: Long): Future[Seq[JsObject]] = {
      fetch(s"/gists/$gistId/forks").get.map(_.json).map{ json =>
        json.as[JsArray].value.map{ fork =>
          fork.transform(cleanJson).getOrElse(JsNull).as[JsObject]
        }
      }
    }
  }
}
