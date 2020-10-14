package io.github.yoyama.digdag.client.config

import java.io.IOException
import java.net.URI
import java.nio.file.{Files, Path}
import java.util.Properties

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.Try
import scala.util.Using
import scala.util.control.Exception.catching
import io.github.yoyama.digdag.client.AuthConfigNone

import scala.concurrent.duration.FiniteDuration

case class ConnectionConfig(name: String, endPoint: URI, auth: AuthConfig, apiWait: FiniteDuration) {
  def apiEndPoint(uriPart: String): String = endPoint.toString + uriPart
}

object ConnectionConfig {
  def apply(name: String, uri: String, auth: AuthConfig = AuthConfigNone(), apiWait: FiniteDuration = 30 second) = new ConnectionConfig(name, new URI(uri), auth, apiWait)

  def local = ConnectionConfig("local", "http://localhost:65432")

  def load(path: Path): Try[ConnectionConfig] = {
    ???
  }

  private def loadProperties(path: Path): Try[Properties] = {
    catching(classOf[IOException]) withTry {
      val props = new Properties
      Using(Files.newInputStream(path)) { in =>
        props.load(in)
      }
      props
    }
  }

  def loadAll(dir: Path): Map[String, Try[ConnectionConfig]] = {
    ???
  }

  def loadAll(): Map[String, Try[ConnectionConfig]] = {
    ???
  }
}