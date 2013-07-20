package services.search

import org.joda.time.DateTime
import scala.concurrent._
import scala.concurrent.duration._

import play.api._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import org.elasticsearch.node._
import org.elasticsearch.node.NodeBuilder._

object EsBuilder extends EsAPI{

  private var node: Option[Node] = None

  def start {

    import org.elasticsearch.common.settings._
    import org.elasticsearch.common.io.stream._
    import java.io._

    if( isEmbedded ){
      play.Logger.info("Starting Local ES")

      import org.elasticsearch.common.settings.loader.SettingsLoader

      val settings = Play.resourceAsStream("elasticsearch.yaml").map{ s =>
        ImmutableSettings.settingsBuilder().loadFromStream("elasticsearch.yaml", s).build
      }

      val n = nodeBuilder().clusterName(clusterName).local(true)

      node = Some(settings.map(n.settings _).getOrElse(n).node)
    } else {
      play.Logger.info(s"Creating index: $indexName")
      val resp = Await.result(
        getIndex(indexName).flatMap{
          case Left(error) =>
            if(error.contains("IndexMissingException")) createIndex(indexName)
            else throw new RuntimeException("Could create index: "+error)
          case Right(settings) => play.Logger.info(s"Index already existing: $settings"); Future.successful()
        },
        Duration("10 seconds")
      )

    }
  }

  def stop() {
    if( isEmbedded ){
      play.Logger.info("Stopping ES")
      for(n <- node) n.stop
    }
  }

}