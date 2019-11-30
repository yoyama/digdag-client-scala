package io.github.yoyama.digdag.client.http

import java.io.File
import java.nio.file.Path

import scala.util.control.Exception._
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.Uri.{Query => AkkaQuery}
import akka.http.scaladsl.model.headers.{RawHeader => AkkaRawHeader}
import akka.http.scaladsl.model.{ErrorInfo, Multipart, RequestEntity, ContentType => AkkaContentType, ContentTypes => AkkaContentTypes, HttpEntity => AkkaHttpEntity, HttpMessage => AkkaHttpMessage, HttpMethods => AkkaHttpMethods, HttpRequest => AkkaHttpRequest, HttpResponse => AkkaHttpResponse, Uri => AkkaUri}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.MediaType
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.http.scaladsl.{Http, model => akkaModel}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import wvlet.airframe.http.{HttpMethod, HttpRequest, HttpRequestAdapter, HttpResponse, HttpResponseAdapter, HttpStatus}
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

trait HttpClientDigdag[REQ,RESP] {
  implicit val ec:ExecutionContext


  def reqAdapter:HttpRequestAdapter[REQ]
  def respAdapter:HttpResponseAdapter[RESP]

  def callGet(uri: String, queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpResponse[RESP]] = {
    val r = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- sendRequest(reqAdapter.httpRequestOf(req))
    } yield resp
    r
  }

  def callPost(uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map()) : Future[HttpResponse[RESP]] = {
    val r = for {
      req <- createRequest("POST",  uri, queries, headers, Option(contentType), Option(content))
      resp <- sendRequest(reqAdapter.httpRequestOf(req))
    } yield resp
    r
  }

  def callPut(uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map()) : Future[HttpResponse[RESP]] = {
    val r = for {
      req <- createRequest("PUT",  uri, queries, headers, Option(contentType), Option(content))
      resp <- sendRequest(reqAdapter.httpRequestOf(req))
    } yield resp
    r
  }

  def callPutFile(uri: String, contentType:String, contentPath:Path,
              queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpResponse[RESP]] = {
    val r = for {
      req <- createRequest("PUT",  uri, queries, headers, contentType, contentPath, 1000*1000)
      resp <- sendRequest(reqAdapter.httpRequestOf(req))
    } yield resp
    r
  }


  def callDelete(uri: String, queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpResponse[RESP]] = {
    val r = for {
      req <- createRequest("DELETE",  uri, queries, headers, None, None)
      resp <- sendRequest(reqAdapter.httpRequestOf(req))
    } yield resp
    r
  }

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String] = Map.empty,
                              contentType: Option[String] = None, body: Option[String] = None): Future[REQ]

  protected def createRequest(method: String, uri: String, queries: Map[String, String],
                              headers: Map[String, String],
                              contentType: String, contentPath: Path, chunkSize:Int): Future[REQ]

  protected def sendRequest(request: HttpRequest[REQ]):Future[HttpResponse[RESP]]
}

class HttpClientAkkaHttp() (implicit val actorSystem:ActorSystem, val mat: ActorMaterializer,
                        val execContext:ExecutionContext, val timeout: Duration)
                                  extends HttpClientDigdag[AkkaHttpRequest, AkkaHttpResponse] {
  import io.github.yoyama.digdag.client.commons.Helpers._
  override val ec = execContext

  val adapters = new HttpClientAdapters4AkkaHttp
  override def reqAdapter:HttpRequestAdapter[AkkaHttpRequest] = adapters.HttpRequestAkkaHttpAdapter
  override def respAdapter:HttpResponseAdapter[AkkaHttpResponse] = adapters.HttpResponseAkkaHttpAdapter


  override protected def createRequest(method: String, uri: String, queries: Map[String, String], headers: Map[String, String],
                              contentType: Option[String], body: Option[String]): Future[AkkaHttpRequest] = {
    val ret = for {
      m <- AkkaHttpMethods.getForKey(method).toFuture(s"Invalid method: ${method}")
      h <- Option(rawHeaders(headers)).toFuture(s"Invalid headers")
      t <- procContentType(contentType).toFuture()
      e <- procBody(body, t).toFuture()
      q <- allCatch either AkkaQuery(queries) toFuture()
      u <- allCatch either AkkaUri(uri) toFuture()
    } yield AkkaHttpRequest(method = m, uri = u.withQuery(q), entity = e).withHeaders(h: _*)
    ret
  }

  override protected def createRequest(method: String, uri: String, queries: Map[String, String],
                                       headers: Map[String, String],
                                       contentType: String, contentPath: Path, chunkSize:Int): Future[AkkaHttpRequest] = {
    val ret = for {
      m <- AkkaHttpMethods.getForKey(method).toFuture(s"Invalid method: ${method}")
      h <- allCatch either rawHeaders(headers) toFuture()
      e <- createEntity(contentPath, contentType, chunkSize)
      q <- allCatch either AkkaQuery(queries) toFuture()
      u <- allCatch either AkkaUri(uri) toFuture()
    } yield AkkaHttpRequest(method = m, uri = u.withQuery(q), entity = e).withHeaders(h: _*)
    ret
  }

  protected def createEntity(path:Path, contentType:String = "application/octet-stream",
                             chunkSize:Int = 1000*1000, bodyName:String = "data" ):Future[RequestEntity] = {
    val ret = for {
      contentType    <- AkkaContentType.parse(contentType).toFuture()
      httpEntity     <- allCatch either AkkaHttpEntity(contentType, path.toFile.length, FileIO.fromPath(path, chunkSize = chunkSize)) toFuture()
      bodyPart      <- allCatch either Multipart.FormData.BodyPart(bodyName, httpEntity) toFuture()
      data:Multipart <- allCatch either Multipart.FormData(Source.single(bodyPart)) toFuture()
      rentity <- Marshal(data).to[RequestEntity]
    } yield rentity
    ret
  }



  override protected def sendRequest(request: HttpRequest[AkkaHttpRequest]): Future[HttpResponse[AkkaHttpResponse]] = {
    val response: Future[HttpResponse[AkkaHttpResponse]] = Http()
      .singleRequest(request.toRaw)
      .map((r: AkkaHttpResponse) => respAdapter.httpResponseOf(r))
    response
  }

  protected def procContentType(ct: Option[String]): Either[Throwable, AkkaContentType] = {
    val t = new Throwable("Invalid content type")
    ct.map(
      AkkaContentType.parse(_).left.map(_ => t)
    ).getOrElse(
      Some(AkkaContentTypes.NoContentType).toRight[Throwable](t)
    )
  }

  protected def procBody(bd: Option[String], ct:AkkaContentType): Either[Throwable, AkkaHttpEntity.Strict] = {
    val t = new Throwable("Invalid body")
    bd.map(
      x => AkkaHttpEntity.apply(ct, ByteString(x))
    ).orElse(
      Some(AkkaHttpEntity.Empty)
    ).toRight[Throwable](t)
  }

  protected def rawHeaders(headers: Map[String, String]): Seq[AkkaRawHeader] = {
    headers.map {
      case (k, v) => AkkaRawHeader(k, v)
    }.toSeq
  }

  implicit class EitherOps[L <: Throwable, R](either: Either[L, R]) {
    def toTry(): Try[R] = {
      val ret: Try[R] = either match {
        case Right(x) => Success(x)
        case Left(y) => Failure(y)
      }
      ret
    }
  }
}

class HttpClientAdapters4AkkaHttp()(implicit val mat:ActorMaterializer, val timeout:Duration) {
  implicit object HttpRequestAkkaHttpAdapter extends HttpRequestAdapter[AkkaHttpRequest] {
    import AkkaHttpHelper._
    override def methodOf(request: AkkaHttpRequest): HttpMethod          = method(request.method)
    override def pathOf(request: AkkaHttpRequest): String                = request.uri.toString()
    override def queryOf(request: AkkaHttpRequest): Map[String, String]  = request.uri.query().toMap
    override def headerOf(request: AkkaHttpRequest): Map[String, String] = request.headers.map(h => (h.name(), h.value())).toMap
    override def contentStringOf(request: AkkaHttpRequest): String       = convertEntity[String](request.entity)
    override def contentBytesOf(request: AkkaHttpRequest): Array[Byte]   = convertEntity[Array[Byte]](request.entity)
    override def contentTypeOf(request: AkkaHttpRequest): Option[String] = contentType(request)
    override def httpRequestOf(request: AkkaHttpRequest): HttpRequest[AkkaHttpRequest] = {
      new HttpRequest[AkkaHttpRequest] {
        override def adapter: HttpRequestAdapter[AkkaHttpRequest] = HttpRequestAkkaHttpAdapter
        override def toRaw: AkkaHttpRequest = request
      }
    }
    override def requestType: Class[akkaModel.HttpRequest] = classOf[akkaModel.HttpRequest]
  }

  implicit object HttpResponseAkkaHttpAdapter extends HttpResponseAdapter[AkkaHttpResponse] {
    import AkkaHttpHelper._
    override def statusOf(resp: AkkaHttpResponse): HttpStatus = HttpStatus.ofCode(resp.status.intValue())
    override def statusCodeOf(resp: AkkaHttpResponse): Int = resp.status.intValue()
    override def contentStringOf(resp: AkkaHttpResponse): String = convertEntity[String](resp.entity)
    override def contentBytesOf(resp: AkkaHttpResponse): Array[Byte] = convertEntity[Array[Byte]](resp.entity)
    override def contentTypeOf(resp: AkkaHttpResponse): Option[String] = contentType(resp)
    override def httpResponseOf(resp: AkkaHttpResponse): HttpResponse[AkkaHttpResponse] = {
      new HttpResponse[AkkaHttpResponse] {
        override protected def adapter: HttpResponseAdapter[AkkaHttpResponse] = HttpResponseAkkaHttpAdapter
        override def toRaw: AkkaHttpResponse = resp
      }
    }
  }

  implicit object AkkaHttpHelper {
    def convertEntity[T](entity: AkkaHttpEntity)(implicit um: Unmarshaller[AkkaHttpEntity, T], mat:ActorMaterializer, timeout:Duration): T = {
      Await.result(Unmarshal(entity).to[T], timeout)
    }

    def contentType(message: AkkaHttpMessage): Option[String] = message.entity.contentType.value match {
      case null | "" | "none/none" => None
      case x => Some(x)
    }

    def method(m: akkaModel.HttpMethod): HttpMethod = {
      HttpMethod.valueOf(m.value)
    }
  }
}
