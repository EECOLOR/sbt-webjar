package org.qirx.sbtWebjars

import sbt._
import sbt.Keys._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStep

object WebjarPluginBuild extends Build {

  val updateReadmeVersion: ReleaseStep = { s: State =>
    val contents = IO.read(file("README.md"))

    val p = Project.extract(s)

    //"org.qirx" %% "sbt-webjar" % "version"
    val pattern = "(\"" + p.get(organization) + "\"\\s+%+\\s+\"" + p.get(name) + "\"\\s+%\\s+\")[\\w\\.-]+(\")"

    val newContents = contents.replaceAll(pattern, "$1" + p.get(releaseVersion)(p.get(version)) + "$2")
    IO.write(file("README.md"), newContents)

    val vcs = p.get(versionControlSystem).getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
    vcs.add(file("README.md").getAbsolutePath) !! s.log

    s
  }

  def insertBeforeIn(seq: Seq[ReleaseStep], before: ReleaseStep, step: ReleaseStep) = {
    val (beforeStep, rest) =
      seq.span(_ != before)

    (beforeStep :+ step) ++ rest
  }

}