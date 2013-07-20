package services.search

import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.json.Json._
import scala.concurrent._
import scala.concurrent.duration._

object Startup extends EsAPI{

  lazy val date: DateTime = Await.result(
    super.get("STARTUP", "1").map( _.fold(
      err => {
        val date = DateTime.now
        super.insert("STARTUP", "1", Json.obj( "date" -> date.toString() ) )
             .map( _.fold( err => date, js => date ) )
      },
      js => {
        play.Logger.info("Last Startup "+js.toString)
        Future( new DateTime( ( js \ "_source" \ "date" ).as[String] ) )
      }
    )).flatMap( identity ), 20 seconds
  )

}