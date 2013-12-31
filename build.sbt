import org.qirx.sbtrelease.UpdateVersionInFiles

releaseSettings

name := "sbt-webjar"

sbtPlugin := true

organization := "org.qirx"

UpdateVersionInFiles(file("README.md"))

publishMavenStyle := false

publishTo <<= version(rhinoflyPluginRepo)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

def rhinoflyRepoNameAndUrl(version: String) = {
  val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
  ("Rhinofly Internal " + repo.capitalize + " Repository", "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
}

def rhinoflyPluginRepo(version: String) = {
  val (name, url) = rhinoflyRepoNameAndUrl(version)
  Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}
