package io.github.yoyama.digdag.client.http

import java.io.InputStream
import java.net.HttpURLConnection
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path}

import scalaj.http.HttpConstants.HttpExec
import scalaj.http._
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

case class SimpleHttpRequest[T](method:String, uri: String, body:Option[T] = None, contentType:Option[String] = None,
                                headers: Map[String, String] = Map(), queries: Map[String, String] = Map())

case class SimpleHttpResponse[T](status:String, contentType:Option[String], contentLength:Option[Long], body:Option[T],
                                 headers: Map[String, Seq[String]]) {
  private[this] val statusCodeRegex = """^HTTP.* (\d+) .*""".r

  def statusCode:Option[Int] = status match {
    case statusCodeRegex(code) => Option(code.toInt)
    case _ => None
  }
}

case class SimpleHttpException[T](resp:SimpleHttpResponse[T]) extends RuntimeException {
  override def toString: String = {
    s"SimpleHttpException: ${resp.statusCode}"
  }

}

object SimpleHttpResponse {
  def bodyUpdate[T,U](src:SimpleHttpResponse[T], body:Option[U])
                    = SimpleHttpResponse[U](src.status, src.contentType, src.contentLength, body, src.headers)
}

object SimpleHttpClient {
  def contentTypeToCharset(ctype:String, defaultCharset:Charset = StandardCharsets.UTF_8):Charset = {
    //ToDo implement parse contentType
    StandardCharsets.UTF_8
  }

  implicit val stringConverter = (body:Array[Byte], ctype:Option[String], csize:Option[Long])
                  => new String(body, ctype.map(contentTypeToCharset(_)).getOrElse(StandardCharsets.UTF_8))
  implicit val unitConverter = (body: Array[Byte], ctype:Option[String], csize:Option[Long]) => ()

}

trait SimpleHttpClient {
  import SimpleHttpClient.stringConverter
  implicit val ec:ExecutionContext // = scala.concurrent.ExecutionContext.global

  type RespConverter[A,U] = (A,Option[String],Option[Long])=> U  // (data,content type, size) => converted.

  /**
  implicit val stringConverter = (body:Array[Byte], ctype:Option[String], csize:Option[Long]) => {
    new String(body, ctype.map(contentTypeToCharset(_)).getOrElse(StandardCharsets.UTF_8))
  }
  */


  def checkStatusCode[T](resp:SimpleHttpResponse[T]):Future[SimpleHttpResponse[T]] = {
    resp.statusCode match {
      case Some(status) if status >= 200 && status <= 299 => Future.successful(resp)
      case _ => Future.failed(SimpleHttpException(resp))
    }
  }

  def callGet[U](uri: String, queries: Map[String, String] = Map(), headers: Map[String, String] = Map())
                (implicit conv:RespConverter[Array[Byte],U]) : Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- sendRequest(req)
      resp2 <- checkStatusCode(resp)
      body <- Future(conv(resp2.body.get, resp2.contentType, resp2.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp2, Option(body))
    r
  }

  def callGetString(uri: String, queries: Map[String, String] = Map(), headers: Map[String, String] = Map()): Future[SimpleHttpResponse[String]] = {
    callGet(uri, queries, headers)(stringConverter)
  }

  def callGetDownload(uri: String, queries: Map[String, String] = Map(), headers: Map[String, String] = Map(), out:Path)
                  : Future[SimpleHttpResponse[Path]] = {
    val r = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- sendRequestDownload(req, out)
      resp2 <- checkStatusCode(resp)
    } yield resp2
    r
  }

  def callPost[U](uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map())
                 (implicit conv:RespConverter[Array[Byte],U]): Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("POST",  uri, queries, headers, Option(contentType), Option(content))
      resp <- {
        println(s"before sendRequest ${req.uri}")
        sendRequest(req)
      }
      resp2 <- checkStatusCode(resp)
      body <- {
        println(s"before conv ${resp2.toString()}")
        Future(conv(resp2.body.get, resp2.contentType, resp2.contentLength))
      } //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp2, Option(body))
    r
  }

  def callPostString(uri: String, contentType:String, content:String,
                  queries: Map[String, String] = Map(),
                  headers: Map[String, String] = Map()): Future[SimpleHttpResponse[String]] = {
    callPost(uri, contentType, content, queries, headers)(stringConverter)
  }

  def callPut[U](uri: String, contentType:String, content:String,
              queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map())
                (implicit conv:RespConverter[Array[Byte],U]): Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("PUT",  uri, queries, headers, Option(contentType), Option(content))
      resp <- sendRequest(req)
      resp2 <- checkStatusCode(resp)
      body <- Future(conv(resp2.body.get, resp2.contentType, resp2.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp2, Option(body))
    r
  }

  def callPutString(uri: String, contentType:String, content:String,
                 queries: Map[String, String] = Map(),
                 headers: Map[String, String] = Map())
                : Future[SimpleHttpResponse[String]] = {
    callPut(uri, contentType, content, queries, headers)(stringConverter)
  }
  def callPutUpload(uri: String, contentType:String, contentPath:Path,
                  queries: Map[String, String] = Map(),
                  headers: Map[String, String] = Map()): Future[SimpleHttpResponse[String]] = {
    val r = for {
      req <- createRequest("PUT",  uri, queries, headers, contentType, contentPath, 1000*1000) //chunkSize is not used
      resp <- sendRequestUpload(req)
      resp2 <- checkStatusCode(resp)
      body <- Future(stringConverter(resp2.body.get, resp2.contentType, resp2.contentLength))
    } yield SimpleHttpResponse.bodyUpdate(resp2, Option(body))
    r
  }

  def callDelete[U](uri: String, queries: Map[String, String] = Map(),
                    headers: Map[String, String] = Map())
                   (implicit conv:RespConverter[Array[Byte],U]): Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("DELETE",  uri, queries, headers, None, None)
      resp <- sendRequest(req)
      resp2 <- checkStatusCode(resp)
      body <- Future(conv(resp2.body.get, resp2.contentType, resp2.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp2, Option(body))
    r
  }

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String] = Map.empty,
                              contentType: Option[String] = None, body: Option[String] = None): Future[SimpleHttpRequest[String]] = {
    //ToDo validation
    Future.successful {
      SimpleHttpRequest(method, uri, body, contentType, headers, queries)
    }
  }

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String],
                              contentType: String, contentPath: Path, chunkSize:Int): Future[SimpleHttpRequest[Path]] = {
    //ToDo validation
    Future.successful {
      SimpleHttpRequest(method, uri, Some(contentPath), Some(contentType), headers, queries)
    }
  }

  protected def sendRequest(request: SimpleHttpRequest[String]):Future[SimpleHttpResponse[Array[Byte]]]
  protected def sendRequestUpload(request: SimpleHttpRequest[Path]):Future[SimpleHttpResponse[Array[Byte]]]
  protected def sendRequestDownload(request: SimpleHttpRequest[String], out:Path):Future[SimpleHttpResponse[Path]]

}

class SimpleHttpClientScalaJ(connTimeout:Int = 1000, readTimeOut:Int = 5000) extends SimpleHttpClient with LogSupport {
  override implicit val ec:ExecutionContext = scala.concurrent.ExecutionContext.global

  override protected def sendRequest(request: SimpleHttpRequest[String]): Future[SimpleHttpResponse[Array[Byte]]] = {
    Future {
      val headers = request.contentType.map(h => request.headers + ("Content-Type" -> h)).getOrElse(request.headers)
      val sjreq1 = httpRequest(request.uri, headers, request.queries)
      val sjreq2 = if(request.body.isDefined) sjreq1.postData(request.body.getOrElse("")) else sjreq1
      val sjreq = sjreq2.method(request.method) // This must be set after postData()
      logger.debug(s"sendRequest: ${sjreq.toString}")
      val sjresp: HttpResponse[Array[Byte]] = sjreq.asBytes

      SimpleHttpResponse(
        status = sjresp.statusLine,
        contentType = sjresp.contentType,
        contentLength = sjresp.headers.get("content-length").map(_.head.toInt),
        body = Option(sjresp.body),
        headers = sjresp.headers
      )
    }
  }

  private def httpRequest(uri:String, headers:Map[String,String], params:Map[String,String]): HttpRequest = {
    Http(uri).headers(headers).params(params)
      .options(Seq(
        HttpOptions.followRedirects(true),
        HttpOptions.connTimeout(connTimeout),
        HttpOptions.readTimeout(readTimeOut)
      ))
  }
  override protected def sendRequestUpload(request: SimpleHttpRequest[Path]):Future[SimpleHttpResponse[Array[Byte]]] = {
    val uploadConnectFunc:HttpExec = (sjreq:HttpRequest, conn:HttpURLConnection) => {
      conn.setDoOutput(true)
      conn.connect
      request.body.map(b => Files.copy(b, conn.getOutputStream))
    }

    Future {
      val reqHeaders: Map[String, String] = request.contentType match {
        case Some(ct) => request.headers. + ("Content-Type" -> ct)
        case None => request.headers
      }
      val sjreq = httpRequest(request.uri, reqHeaders, request.queries)
        .copy(connectFunc = uploadConnectFunc)

      val sjresp: HttpResponse[Array[Byte]] = request.method match {
        case "POST" => sjreq.method("POST").asBytes
        case "PUT" => sjreq.method("PUT").asBytes
      }
      SimpleHttpResponse(
        status = sjresp.statusLine,
        contentType = sjresp.contentType,
        contentLength = sjresp.headers.get("content-length").map(_.head.toInt),
        body = Option(sjresp.body),
        headers = sjresp.headers
      )
    }
  }

  override protected def sendRequestDownload(request: SimpleHttpRequest[String], out:Path):Future[SimpleHttpResponse[Path]] = {
    Future {
      val sjreq = httpRequest(request.uri, Map.empty, Map.empty)
      val sjresp: HttpResponse[Path] = request.method match {
        case "GET" => sjreq.headers(request.headers).params(request.queries).execute( (in:InputStream) => {
          assert(in != null)
          assert(out != null)
          Files.copy(in, out)
          out
        })
      }
      SimpleHttpResponse(
        status = sjresp.statusLine,
        contentType = sjresp.contentType,
        contentLength = sjresp.headers.get("content-length").map(_.head.toInt),
        body = Option(sjresp.body),
        headers = sjresp.headers
      )
    }
  }
}
