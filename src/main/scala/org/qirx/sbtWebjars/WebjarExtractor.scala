package org.qirx.sbtWebjars

import sbt._
import sbt.Keys._

/**
 * Contains the logic for handling webjars. Features:
 *
 * - selecting webjars from a list of jars
 * - determining if a jar is a webjar
 * - extract a webjar
 * - collect a list of Webjar instances
 * - retrieving all the files from an extracted webjar
 * - generate a file that contains a list of webjars
 */
object WebjarExtractor {

  def selectWebjars(classpath: Classpath) =
    classpath.collect { case jar if isWebjar(jar) => jar }

  def isWebjar(jar: Attributed[File]) =
    jar.get(moduleID.key).exists(_.organization == "org.webjars")

  def extract(
    extractDir: File,
    include: FileFilter,
    exclude: FileFilter,
    webjars: Seq[Attributed[File]]) = {

    val filter = include -- exclude

    val extractJars = createExtractJarsFunction(extractDir, filter)
    extractJars(webjars.files.toSet)
  }

  def collectWebjars(webjars: Seq[Attributed[File]], extractDir: File, logger: Logger): Seq[Webjar] = {

    val names = webjarNames(webjars)
    val extractedWebjarDirs = getExtractedWebjarDirs(names, extractDir, logger)

    for {
      (name, path) <- extractedWebjarDirs
      webjarDir <- getWebjarDirByName(path, name, logger)
      version <- getVersions(webjarDir, logger)
    } yield Webjar(name, version, path, webjarDir / version)
  }

  def filesInWebjars(webjars: Seq[Webjar]) =
    (webjars.map(_.path) ** "*").get

  def generateWebjarsList(webjars: Seq[Webjar], outputDir: File, outputFile: File): File = {
    val output =
      for (Webjar(name, version, path, resources) <- webjars) yield {
        val Some(relativePath) = IO.relativize(path, resources)
        s"$name;$version;$relativePath"
      }
    IO.write(outputFile, output.mkString("\n"))
    outputFile
  }

  private def createExtractJarsFunction(outputDir: File, filter: FileFilter) = {

    val nameFilter =
      new NameFilter {
        def accept(name: String) = filter.accept(new File(name))
      }

    val jarExtractor = new JarExtractor(outputDir, nameFilter)

    FileFunction.cached(outputDir)(inStyle = FilesInfo.lastModified, outStyle = FilesInfo.exists) {
      (report, _) =>
        jarExtractor.removeDirs(report.modified)
        jarExtractor.extractJars(report.modified -- report.removed)
    }
  }

  private def webjarNames(webjars: Seq[Attributed[File]]): Seq[String] =
    for {
      jar <- webjars
      possibleJarInfo = jar.get(moduleID.key)
      jarInfo <- possibleJarInfo
    } yield jarInfo.name

  private def getExtractedWebjarDirs(names: Seq[String], extractDir: File, logger: Logger) =
    names.flatMap { name =>
      val pattern = name + "-*"
      val extractDirs = extractDir * pattern
      val webjarDir = extractDirs.get.headOption
      if (webjarDir.isEmpty)
        logger.warn(s"Could not find extracted dir for '$name' with pattern '$pattern'")

      webjarDir.map(name -> _)
    }

  private def getWebjarDirByName(extractedWebjarDir: File, name: String, logger: Logger): Seq[File] = {
    val webjarDir = extractedWebjarDir / "META-INF" / "resources" / "webjars" / name
    if (!webjarDir.exists || !webjarDir.isDirectory) {
      logger.warn("Expected a dir at " + webjarDir)
      Seq.empty
    } else Seq(webjarDir)
  }

  private def getVersions(webjarDir: File, logger: Logger): Seq[String] = {
    val possibleVersions = (webjarDir * "*").get
    val versions = possibleVersions.filter(_.isDirectory).map(_.getName)
    if (versions.isEmpty)
      logger.warn("Could not find version dir in " + webjarDir)
    versions
  }
}