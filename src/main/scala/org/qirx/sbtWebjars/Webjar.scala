package org.qirx.sbtWebjars

import java.io.File

/**
 * Representation of a webjar. Note that the version is of the library
 * inside of the webjar, not the webjar version itself.
 *
 * @param name      The name of the webjar
 * @param version   The version of the library in the webjar
 * @param path      The path where the webjar is extracted
 * @param resources The resources inside of the webjar (META-INF/resources/{name}/{version})
 */
case class Webjar(name: String, version: String, path: File, resources:File)