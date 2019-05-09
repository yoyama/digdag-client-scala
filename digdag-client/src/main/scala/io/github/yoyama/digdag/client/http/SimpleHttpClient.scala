package io.github.yoyama.digdag.client.http

import java.io.InputStream
import java.net.HttpURLConnection
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path}

import scalaj.http.HttpConstants.HttpExec
import scalaj.http._

import scala.concurrent.Future

case class SimpleHttpRequest[T](method:String, uri: String, body:Option[T] = None, contentType:Option[String] = None,
                                headers: Map[String, String] = Map(), queries: Map[String, String] = Map())

case class SimpleHttpResponse[T](status:String, contentType:Option[String], contentLength:Option[Long], body:Option[T],
                                 headers: Map[String, Seq[String]])

object SimpleHttpResponse {
  def bodyUpdate[T,U](src:SimpleHttpResponse[T], body:Option[U])
                    = SimpleHttpResponse[U](src.status, src.contentType, src.contentLength, body, src.headers)
}

trait SimpleHttpClient {
  implicit val ec = scala.concurrent.ExecutionContext.global

  type RespConverter[A,U] = (A,Option[String],Option[Long])=> U  // (data,content type, size) => converted.

  implicit val stringConverter = (body:Array[Byte], ctype:Option[String], csize:Option[Long]) => {
    new String(body, ctype.map(contenTypeToCharset(_)).getOrElse(StandardCharsets.UTF_8))
  }

  def contenTypeToCharset(ctype:String, defaultCharset:Charset = StandardCharsets.UTF_8):Charset = {
    //ToDo implement parse contentType
    StandardCharsets.UTF_8
  }

  def callGet[U](uri: String, queries: Map[String, String] = Map(), headers: Map[String, String] = Map())
                (implicit conv:RespConverter[Array[Byte],U]) : Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp, Option(body))
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
    } yield resp
    r
  }

  def callPost[U](uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map())
                 (implicit conv:RespConverter[Array[Byte],U]): Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("POST",  uri, queries, headers, Option(contentType), Option(content))
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp, Option(body))
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
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp, Option(body))
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
      body <- Future(stringConverter(resp.body.get, resp.contentType, resp.contentLength))
    } yield SimpleHttpResponse.bodyUpdate(resp, Option(body))
    r
  }

  def callDelete[U](uri: String, queries: Map[String, String] = Map(),
                    headers: Map[String, String] = Map())
                   (implicit conv:RespConverter[Array[Byte],U]): Future[SimpleHttpResponse[U]] = {
    val r = for {
      req <- createRequest("DELETE",  uri, queries, headers, None, None)
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield SimpleHttpResponse.bodyUpdate(resp, Option(body))
    r
  }

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String] = Map.empty,
                              contentType: Option[String] = None, body: Option[String] = None): Future[SimpleHttpRequest[String]] = {
    //ToDo validation
    Future.successful {
      SimpleHttpRequest(method, uri, None, contentType, headers, queries)
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

class SimpleHttpClientScalaJ extends SimpleHttpClient {
  override protected def sendRequest(request: SimpleHttpRequest[String]): Future[SimpleHttpResponse[Array[Byte]]] = {
    Future {
      val sjreq = Http(request.uri).option(HttpOptions.followRedirects(true))
      val sjresp: HttpResponse[Array[Byte]] = request.method match {
        case "GET" => sjreq.headers(request.headers).params(request.queries).asBytes
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
      val sjreq =
        Http(request.uri)
          .option(HttpOptions.followRedirects(true))
          .headers(reqHeaders)
          .params(request.queries)
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
      val sjreq = Http(request.uri).option(HttpOptions.followRedirects(true))
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
