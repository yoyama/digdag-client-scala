package io.github.yoyama.digdag.client.http

import java.io.InputStream
import java.nio.file.Path

import scalaj.http.{Http, HttpResponse}

import scala.concurrent.Future

case class DigdagHttpRequest[T](method:String, uri: String, body:Option[T] = None, contentType:Option[String] = None,
                                headers: Map[String, String] = Map(), queries: Map[String, String] = Map())

case class DigdagHttpResponse[T](status:String, contentType:Option[String], contentLength:Option[Long], body:Option[T],
                                 headers: Map[String, Seq[String]])

object DigdagHttpResponse {
  def bodyUpdate[T,U](src:DigdagHttpResponse[T], body:Option[U])
                    = DigdagHttpResponse[U](src.status, src.contentType, src.contentLength, body, src.headers)
}

trait DigdagHttpClient {
  implicit val ec = scala.concurrent.ExecutionContext.global

  type RespConverter[A,U] = (A,Option[String],Option[Long])=> U  // (data,content type, size) => converted.

  def callGet[U](uri: String, queries: Map[String, String] = Map(), headers: Map[String, String] = Map())
                (implicit conv:RespConverter[Array[Byte],U]) : Future[DigdagHttpResponse[U]] = {
    val r = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield DigdagHttpResponse.bodyUpdate(resp, Option(body))
    r
  }

  def callGetDownload(uri: String, queries: Map[String, String] = Map(), headers: Map[String, String] = Map(), out:Path)
                  : Future[DigdagHttpResponse[Path]] = {
    val r = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- sendRequestDownload(req, out)
    } yield resp
    r
  }

  def callPost[U](uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map())
                 (implicit conv:RespConverter[Array[Byte],U]): Future[DigdagHttpResponse[U]] = {
    val r = for {
      req <- createRequest("POST",  uri, queries, headers, Option(contentType), Option(content))
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield DigdagHttpResponse.bodyUpdate(resp, Option(body))
    r
  }

  def callPut[U](uri: String, contentType:String, content:String,
              queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map())
                (implicit conv:RespConverter[Array[Byte],U]): Future[DigdagHttpResponse[U]] = {
    val r = for {
      req <- createRequest("PUT",  uri, queries, headers, Option(contentType), Option(content))
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield DigdagHttpResponse.bodyUpdate(resp, Option(body))
    r
  }

  def callPutUpload[U](uri: String, contentType:String, contentPath:Path,
                  queries: Map[String, String] = Map(),
                  headers: Map[String, String] = Map())
                      (implicit conv:RespConverter[Array[Byte],U]): Future[DigdagHttpResponse[U]] = {
    val r = for {
      req <- createRequest("PUT",  uri, queries, headers, contentType, contentPath, 1000*1000)
      resp <- sendRequestUpload(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield DigdagHttpResponse.bodyUpdate(resp, Option(body))
    r
  }
  
  def callDelete[U](uri: String, queries: Map[String, String] = Map(),
                    headers: Map[String, String] = Map())
                   (implicit conv:RespConverter[Array[Byte],U]): Future[DigdagHttpResponse[U]] = {
    val r = for {
      req <- createRequest("DELETE",  uri, queries, headers, None, None)
      resp <- sendRequest(req)
      body <- Future(conv(resp.body.get, resp.contentType, resp.contentLength)) //ToDo error handling
    } yield DigdagHttpResponse.bodyUpdate(resp, Option(body))
    r
  }

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String] = Map.empty,
                              contentType: Option[String] = None, body: Option[String] = None): Future[DigdagHttpRequest[String]] = {
    //ToDo validation
    Future.successful {
      DigdagHttpRequest(method, uri, None, contentType, headers, queries)
    }
  }

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String],
                              contentType: String, contentPath: Path, chunkSize:Int): Future[DigdagHttpRequest[Path]] = {
    //ToDo validation
    Future.successful {
      DigdagHttpRequest(method, uri, Some(contentPath), Some(contentType), headers, queries)
    }
  }

  protected def sendRequest(request: DigdagHttpRequest[String]):Future[DigdagHttpResponse[Array[Byte]]]
  protected def sendRequestUpload(request: DigdagHttpRequest[Path]):Future[DigdagHttpResponse[Array[Byte]]]
  protected def sendRequestDownload(request: DigdagHttpRequest[String], out:Path):Future[DigdagHttpResponse[Path]]

}

class DigdagHttpClientScalaJ extends DigdagHttpClient {
  override protected def sendRequest(request: DigdagHttpRequest[String]): Future[DigdagHttpResponse[Array[Byte]]] = {
    Future {
      val sjreq = Http(request.uri)
      val sjresp: HttpResponse[Array[Byte]] = request.method match {
        case "GET" => sjreq.headers(request.headers).params(request.queries).asBytes
      }
      DigdagHttpResponse(
        status = sjresp.statusLine,
        contentType = sjresp.contentType,
        contentLength = sjresp.headers.get("content-length").map(_.head.toInt),
        body = Option(sjresp.body),
        headers = sjresp.headers
      )
    }
  }

  override protected def sendRequestUpload(request: DigdagHttpRequest[Path]):Future[DigdagHttpResponse[Array[Byte]]] = {
    ???
  }

  protected def sendRequestDownload(request: DigdagHttpRequest[String], out:Path):Future[DigdagHttpResponse[Path]] = {
    Future {
      val sjreq = Http(request.uri)
      val sjresp: HttpResponse[Path] = request.method match {
        case "GET" => sjreq.headers(request.headers).params(request.queries).execute( (in:InputStream) => {
          //ToDo store to out
          out
        })
      }
      DigdagHttpResponse(
        status = sjresp.statusLine,
        contentType = sjresp.contentType,
        contentLength = sjresp.headers.get("content-length").map(_.head.toInt),
        body = Option(sjresp.body),
        headers = sjresp.headers
      )
    }
  }
}