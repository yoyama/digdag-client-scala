package io.github.yoyama.digdag.client

import java.nio.charset.StandardCharsets

import wvlet.airframe.http._

import scala.concurrent.Future
import scala.reflect.runtime.universe

case class HttpRequestDigdag(val method: String,
                             val path: String,
                             val contentType: Option[String] = None,
                             val header: Map[String, String] = Map.empty,
                             val query: Map[String, String] = Map.empty,
                             val contentString: Option[String] = None)

case class HttpResponseDigdag(val status: Int,
                              val header: Map[String, String] = Map.empty,
                              val contentString: String = "",
                              val contentType: Option[String] = None)

trait HttpClientDigdag {
  def callGet(uri: String,
              queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()
             ) : Future[HttpRequestDigdag]
  def sendRequest(request: HttpRequestDigdag):Future[HttpResponseDigdag]
}

class HttpClientDigdagAirframe extends HttpClientDigdag
{
  val client = new HttpClient[Future, HttpRequestDigdag, HttpResponseDigdag] {}
  override def callGet(uri: String, queries: Map[String, String], headers: Map[String, String]): Future[HttpRequestDigdag] = ???

  override def sendRequest(request: HttpRequestDigdag): Future[HttpResponseDigdag] = ???
}



object HttpDigdagClientAdapterAirframe {

  implicit object HttpRequestDigdagAdapter extends HttpRequestAdapter[HttpRequestDigdag] {
    override def methodOf(request: HttpRequestDigdag): HttpMethod          = request.method.toHttpMethod
    override def pathOf(request: HttpRequestDigdag): String                = request.path
    override def queryOf(request: HttpRequestDigdag): Map[String, String]  = request.query
    override def headerOf(request: HttpRequestDigdag): Map[String, String] = request.header
    override def contentStringOf(request: HttpRequestDigdag): String       = request.contentString.getOrElse("")
    override def contentBytesOf(request: HttpRequestDigdag): Array[Byte]   = request.contentString.getOrElse("").toByteArray
    override def contentTypeOf(request: HttpRequestDigdag): Option[String] = request.contentType
    override def httpRequestOf(request: HttpRequestDigdag): HttpRequest[HttpRequestDigdag] = {
      new HttpRequest[HttpRequestDigdag] {
        override def adapter: HttpRequestAdapter[HttpRequestDigdag] = HttpRequestDigdagAdapter
        override def toRaw: HttpRequestDigdag = request
      }
    }
    override def requestType: Class[HttpRequestDigdag] = classOf[HttpRequestDigdag]
  }

  implicit object HttpResponseDigdagAdapter extends HttpResponseAdapter[HttpResponseDigdag] {
    override def statusOf(resp: HttpResponseDigdag): HttpStatus = HttpStatus.ofCode(resp.status)
    override def statusCodeOf(resp: HttpResponseDigdag): Int = resp.status
    override def contentStringOf(resp: HttpResponseDigdag): String = resp.contentString
    override def contentBytesOf(resp: HttpResponseDigdag): Array[Byte] = resp.contentString.toByteArray
    override def contentTypeOf(resp: HttpResponseDigdag): Option[String] = resp.contentType
    override def httpResponseOf(resp: HttpResponseDigdag): HttpResponse[HttpResponseDigdag] = {
      new HttpResponse[HttpResponseDigdag] {
        override protected def adapter: HttpResponseAdapter[HttpResponseDigdag] = HttpResponseDigdagAdapter
        override def toRaw: HttpResponseDigdag = resp
      }
    }
  }

  implicit class HttpStringConverter(val str:String) {
    def toHttpMethod: HttpMethod = str match {
      case s if ("get".equalsIgnoreCase(s)) => HttpMethod.GET
      case s if ("put".equalsIgnoreCase(s)) => HttpMethod.PUT
      case s if ("post".equalsIgnoreCase(s)) => HttpMethod.POST
      case s if ("head".equalsIgnoreCase(s)) => HttpMethod.HEAD
      case s if ("delete".equalsIgnoreCase(s)) => HttpMethod.DELETE
      case s if ("patch".equalsIgnoreCase(s)) => HttpMethod.PATCH
      case s if ("options".equalsIgnoreCase(s)) => HttpMethod.OPTIONS
      case s if ("trace".equalsIgnoreCase(s)) => HttpMethod.TRACE
    }

    def toByteArray: Array[Byte] = str.getBytes(StandardCharsets.UTF_8)
  }
}

