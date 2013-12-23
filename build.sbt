import ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._

releaseSettings

name := "sbt-webjar"

sbtPlugin := true

organization := "org.qirx"

// `insertBeforeIn` and `updateReadmeVersion` are defined in Build.scala
releaseProcess :=
  insertBeforeIn(releaseProcess.value,
    before = commitReleaseVersion,
    step = updateReadmeVersion)

publishTo <<= version(rhinoflyRepo)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := false

def rhinoflyRepo(version: String) = {
  val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
  Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
}
