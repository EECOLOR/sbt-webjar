package org.qirx.sbtWebjars

import sbt._
import sbt.Keys._

/**
 * The plugin definition.
 *
 * Contains the keys and default settings.
 */
object WebjarPlugin extends Plugin {

  object WebjarKeys {
    val webjarDependencies =
      taskKey[Seq[Attributed[File]]]("The webjars this project depends upon")

    val extractWebjars =
      taskKey[File]("Extracts webjars, value contain the directory where the jars were extracted")

    val webjars =
      taskKey[Seq[Webjar]]("The available webjars")

    val generateWebjarList =
      taskKey[File]("Generates a list of webjars in a file")
  }

  import WebjarKeys._

  val baseWebjarSettings: Seq[Setting[_]] = Seq(

    webjarDependencies := WebjarExtractor.selectWebjars(
          classpath = (managedClasspath in webjarDependencies).value),

    managedClasspath in webjarDependencies := managedClasspath.value,

    extractWebjars := {
      val extractDir = streams.value.cacheDirectory
      WebjarExtractor.extract(
        extractDir,
        include = (includeFilter in extractWebjars).value,
        exclude = (excludeFilter in extractWebjars).value,
        webjars = webjarDependencies.value)
      extractDir
    },

    includeFilter in extractWebjars := "*",
    excludeFilter in extractWebjars := NothingFilter,

    webjars :=
      WebjarExtractor.collectWebjars(
          webjars = webjarDependencies.value,
          extractDir = extractWebjars.value,
          logger = streams.value.log),

    generateWebjarList :=
      WebjarExtractor.generateWebjarsList(
        webjars = webjars.value,
        outputDir = (target in extractWebjars).value,
        outputFile = (target in generateWebjarList).value),

    target in generateWebjarList := resourceManaged.value / "webjars.list",

    resourceGenerators <+= generateWebjarList.map(Seq(_))
  )

  def webjarSettingsIn(c: Configuration) =
    inConfig(c)(baseWebjarSettings)

  val webjarSettings =
    webjarSettingsIn(Compile) ++
      webjarSettingsIn(Test) ++
      webjarSettingsIn(Runtime)

}