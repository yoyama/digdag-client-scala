package io.github.yoyama.digdag.client.config

import scala.jdk.CollectionConverters._
import java.io.IOException
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import java.util.Properties

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.{Success, Try, Using}
import scala.util.control.Exception.catching
import scala.concurrent.duration.FiniteDuration
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, TryHelper}

case class ConnectionConfig(name: String, endPoint: URI, auth: AuthConfig, headers: Map[String, String], apiConnWait: FiniteDuration, apiReadWait: FiniteDuration) {
  def apiEndPoint(uriPart: String): String = endPoint.toString + uriPart
}

object ConnectionConfig {
  def apply(name: String, endPoint: String, auth: AuthConfig = AuthConfigNone(),
            headers: Map[String, String] = Map(),
            apiConnWait: FiniteDuration = 30 second, apiReadWait: FiniteDuration = 30 second)
          = new ConnectionConfig(name, new URI(endPoint), auth, headers, apiConnWait, apiReadWait)

  def localEndpoint = "http://localhost:65432"
  def local = ConnectionConfig("local", localEndpoint)

  def loadAll(): Try[Map[String, ConnectionConfig]] = {
    val homeDir = Paths.get(System.getProperty("user.home"))
    val digdagConfigDir = homeDir.resolve(".config").resolve("digdag")
    loadAll(digdagConfigDir)
  }

  def loadAll(dir: Path): Try[Map[String, ConnectionConfig]] = {
    for {
      configPaths <- listConfig(dir)
      configList <- Success(configPaths.map(load(_)).filter(_.isSuccess).map(_.get))
      configMap <- Success(configList.map(c => (c.name, c)).toMap)
    } yield configMap
  }

  def load(path: Path): Try[ConnectionConfig] = {
    for {
      props <- loadProperties(path)
      config <- props2Config(path, props)
    } yield config
  }

  protected def listConfig(dir:Path):Try[Seq[Path]] = {
    def listConfigR(dir:Path): Seq[Path] = {
      dir.toFile.listFiles().foldLeft(Seq[Path]()){ (acc, v) =>
        v match {
          case v1 if v1.isDirectory && v1.getName() != "plugins" => acc ++ listConfigR(v1.toPath)
          case v1 if v1.isFile && v1.getName().matches(".*~") => acc
          case v1 if v1.isFile && v1.getName().matches(".*config.*") => acc :+ v1.toPath
          case _ => acc
        }
      }
    }
    if (Files.exists(dir))
      Success(listConfigR(dir))
    else
      Success(Seq.empty)
  }

  protected def props2Config(path:Path, props:Properties):Try[ConnectionConfig] = {
    for {
      configName <- getConfigName(path, props)
      endPoint <- Option(props.getProperty("client.http.endpoint", localEndpoint)).toTry("No endpoint")
      authConfig <- getAuthConfig(props)
      headers <- getHeaders(props)
      timeouts <- getHttpTimeout(props)
    } yield ConnectionConfig(
        configName, endPoint, authConfig, headers,
        timeouts._1.getOrElse(60 seconds), timeouts._2.getOrElse(60 seconds)
    )
  }

  protected def getConfigName(path:Path, props:Properties):Try[String] = {
    (Option(props.getProperty("client.config_name")), path.getFileName.toString, path.getParent.getFileName.toString) match {
      case (Some(cn), _, _) => Success(cn)
      case (_, "config", pn) => Success(pn)
      case (_, cn, _) => Success(cn)
    }
  }

  protected def getAuthConfig(props:Properties):Try[AuthConfig] = {
    Option(props.getProperty("client.http.headers.authorization")) match {
      case None => Success(AuthConfigNone())
      case Some(v) => Success(AuthConfigRaw(v))
    }
  }

  protected def getHeaders(props:Properties):Try[Map[String,String]] = {
    val headerRegex = """^client\.http\.headers\.(.*)""".r
    val headers = props.propertyNames().asScala.foldLeft(Map[String,String]()){ (acc ,pn) =>
      pn.toString match {
        case headerRegex(k) if k.toLowerCase() != "authorization" => acc + (k -> props.getProperty(pn.toString))
        case _ => acc
      }
    }
    Success(headers)
  }

  protected def getHttpTimeout(props:Properties):Try[(Option[FiniteDuration], Option[FiniteDuration])] = {
    val connTimeout = Option(props.getProperty("client.http.timeout.connection"))
      .map(ms => FiniteDuration(ms.toLong, SECONDS))
    val readTimeout = Option(props.getProperty("client.http.timeout.read"))
      .map(ms => FiniteDuration(ms.toLong, SECONDS))
    Success(connTimeout, readTimeout)
  }

  protected def loadProperties(path: Path): Try[Properties] = {
    catching(classOf[IOException]) withTry {
      val props = new Properties
      Using(Files.newInputStream(path)) { in =>
        props.load(in)
      }
      props
    }
  }
}
