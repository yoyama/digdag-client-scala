package io.github.yoyama.digdag.client

import java.net.URI
import java.nio.file.Path

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.Try

trait AuthConfig {
  def authHeader():Option[String]
}

case class AuthConfigNone() extends AuthConfig {
  override def authHeader(): Option[String] = None
}

case class AuthConfigBasic(user:String, pass:String) extends AuthConfig {
  import java.util.Base64
  override def authHeader(): Option[String] = {
    val b = Base64.getEncoder.encode(s"${user}:${pass}".getBytes)
    Some(s"Basic ${new String(b)}")
  }
}

case class AuthConfigRaw(header:String) extends AuthConfig {
  override def authHeader(): Option[String] = Some(header)
}

case class ConnectionConfig(name:String, endPoint:URI, auth:AuthConfig, apiWait:FiniteDuration) {
  def apiEndPoint(uriPart:String): String = endPoint.toString + uriPart
}

object ConnectionConfig {
  def apply(name:String, uri:String, auth:AuthConfig = AuthConfigNone(), apiWait:FiniteDuration = 30 second) = new ConnectionConfig(name, new URI(uri), auth, apiWait)

  def local = ConnectionConfig("local", "http://localhost:65432")

  def load(path:Path): Try[ConnectionConfig] = {
    ???
  }

  def loadAll(dir:Path): Map[String, Try[ConnectionConfig]] = {
    ???
  }

  def loadAll(): Map[String, Try[ConnectionConfig]] = {
    ???
  }
}

