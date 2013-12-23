package org.qirx.sbtWebjars

import sbt._

/**
 * Utility class to help with extracting jar files
 */
class JarExtractor(
  outputDir: File,
  filter: NameFilter) {

  def name(jar: File) = jar.getName

  def extractJars(jars: Set[File]): Set[File] =
    jars.flatMap { jar =>
      val targetDir = outputDir / name(jar)
      IO.createDirectory(targetDir)
      IO.unzip(jar, targetDir, filter, preserveLastModified = true)
    }

  def removeDirs(jars: Set[File]): Unit =
    for (jar <- jars) {
      val targetDir = outputDir / name(jar)
      if (targetDir.exists) IO.delete(targetDir)
    }
}