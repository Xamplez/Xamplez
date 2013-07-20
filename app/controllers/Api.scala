package controllers

import scala.concurrent.Future
import scala.collection.JavaConverters._

import akka.actor.{Actor, Props}

import play.api._
import play.api.mvc._
import services.github._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._

import play.libs.Akka

import play.api.Play.current

object Api extends GithubOAuthController {
  lazy val admins = Play.application.configuration.getStringList("admins").map(_.asScala).getOrElse(List())

  val indexer = Akka.system.actorOf(Props(new actors.Indexer), name = "updater")

  def updateIndex = Authenticated { authreq  =>
    Async{
      GithubWS.User.me(authreq.token).map { user =>
        val login = (user \ "login").as[String]
        play.Logger.debug("login:"+login+ " admins:"+admins)
        if(admins.contains(login)){
          indexer ! "updating"
          Ok("Launched updating")
        }
        else Forbidden("You can't do that dude!")
      }
    }
  }

  /*private def getLanguageField(name: String, field: String) = (__ \ name).json.copyFrom(
    (__ \ "files").json.pick[JsObject].map{ js =>
      js.fields.collectFirst{ case (k, v) if(v \ "language" != JsNull) => v \ field }.get
    }
  )

  private def getQuestionField(name: String, field: String) = (__ \ name).json.copyFrom(
    (__ \ "files").json.pick[JsObject].map{ js =>
      js.fields.collectFirst{ case (k, v) if(v \ "language" == JsNull) => v \ field }.get
    }
  )

  private val fullTransfForks = (
    (__ \ "url").json.pickBranch and
    (__ \ "id").json.pickBranch and
      getQuestionField("questionUrl", "raw_url") and
      getQuestionField("questionContent", "content") and
      getLanguageField("answerUrl", "raw_url") and
      getLanguageField("answerContent", "content")
    ).reduce

  def listForks(gistId: Long) = Authenticated { implicit req =>
    Async {
      GithubWS.Gist.listForks(gistId).map(listJson =>
        Ok(Json.toJson(listJson))
      )
    }
  }

  def getQuestion(gistId: Long) = Authenticated { implicit req =>
    Async {
      GithubWS.Gist.get(gistId).map(json =>
        Ok(json.transform(
          ( getQuestionField("questionUrl", "raw_url") and
            getQuestionField("questionContent", "content")
          ).reduce).getOrElse(JsNull))
      )
    }
  }*/
}