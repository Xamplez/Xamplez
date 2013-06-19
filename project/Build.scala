import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "playbyexample"
  val appVersion      = "1.0-SNAPSHOT"

  val mandubianRepo = Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
  )

  val appDependencies = Seq(
    "org.elasticsearch" % "elasticsearch" % "0.20.5",
    "play-json-zipper"  %% "play-json-zipper"    % "0.1-SNAPSHOT"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= mandubianRepo,
    lessEntryPoints <<= baseDirectory(_ / "app" / "assets" / "stylesheets" ** "main.less")
  )

}
