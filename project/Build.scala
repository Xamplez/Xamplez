import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "playbyexample"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.elasticsearch" % "elasticsearch" % "0.20.5"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    lessEntryPoints <<= baseDirectory(_ / "app" / "assets" / "stylesheets" ** "main.less")
  )

}
