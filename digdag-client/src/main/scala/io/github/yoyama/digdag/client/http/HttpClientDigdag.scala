package io.github.yoyama.digdag.client.http

import scala.util.control.Exception._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.{Query => AkkaQuery}
import akka.http.scaladsl.model.headers.{RawHeader => AkkaRawHeader}
import akka.http.scaladsl.model.{ContentType => AkkaContentType, ContentTypes => AkkaContentTypes, HttpEntity => AkkaHttpEntity, HttpMessage => AkkaHttpMessage, HttpMethod => AkkaHttpMethod, HttpMethods => AkkaHttpMethods, HttpRequest => AkkaHttpRequest, HttpResponse => AkkaHttpResponse, MediaTypes => AkkaMediaTypes, Uri => AkkaUri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.http.scaladsl.{Http, model => akkaModel}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import wvlet.airframe.http.{HttpMethod, HttpRequest, HttpRequestAdapter, HttpResponse, HttpResponseAdapter, HttpStatus}
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

trait HttpClientDigdag[REQ,RES] {
  def callGet(uri: String, queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpResponse[RES]]

  def callPost(uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map()) : Future[HttpResponse[RES]]

  def callPut(uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map()) : Future[HttpResponse[RES]]

  def callDelete(uri: String, queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpResponse[RES]]


  def sendRequest(request: HttpRequest[REQ]):Future[HttpResponse[RES]]
}

class HttpClientAkkaHttp() (implicit val actorSystem:ActorSystem, val mat: ActorMaterializer,
                        val execContext:ExecutionContext, val timeout: Duration)
  extends HttpClientDigdag[AkkaHttpRequest, AkkaHttpResponse] {

  val adapters = new HttpClientAdapterAkkaHttp

  import adapters.{HttpResponseAkkaHttpAdapter => respAdapter}
  import adapters.{HttpRequestAkkaHttpAdapter => reqAdapter}

  override def callGet(uri: String, queries: Map[String, String] = Map(),
                       headers: Map[String, String] = Map()): Future[HttpResponse[AkkaHttpResponse]] = {
    val r: Try[Future[HttpResponse[AkkaHttpResponse]]] = for {
      req <- createRequest("GET", uri, queries, headers, None, None)
      resp <- (catching(classOf[Throwable]) either sendRequest(reqAdapter.httpRequestOf(req))).toTry
    } yield resp

    r match {
      case Success(value) => value
      case Failure(e) => Future.failed(e)
    }
  }

  override def callPost(uri: String, contentType:String, content:String,
               queries: Map[String, String] = Map(),
               headers: Map[String, String] = Map()) : Future[HttpResponse[AkkaHttpResponse]] = ???

  override def callPut(uri: String, contentType:String, content:String,
              queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpResponse[AkkaHttpResponse]] = ???

  override def callDelete(uri: String, queries: Map[String, String] = Map(),
                 headers: Map[String, String] = Map()) : Future[HttpResponse[AkkaHttpResponse]] = ???

  protected def createRequest(method: String, uri: String, queries: Map[String, String], headers: Map[String, String],
                              contentType: Option[String] = None, body: Option[String] = None): Try[AkkaHttpRequest] = {
    val ret = for {
      m <- AkkaHttpMethods.getForKey(method).toRight[Throwable](new Throwable(s"Invalid method: ${method}"))
      h <- Option(rawHeaders(headers)).toRight[Throwable](new Throwable(s"Invalid headers"))
      t <- procContentType(contentType)
      e <- procBody(body, t)
      q <- Option(AkkaQuery(queries)).toRight[Throwable](new Throwable(s"Invalid queries"))
      u <- Option(AkkaUri(uri)).toRight[Throwable](new Throwable("Invalid uri"))
    }
      yield AkkaHttpRequest(method = m, uri = u.withQuery(q), entity = e).withHeaders(h: _*)
    ret.toTry
  }

  override def sendRequest(request: HttpRequest[AkkaHttpRequest]): Future[HttpResponse[AkkaHttpResponse]] = {
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

  implicit class TryFutureOps[T](op: Try[Future[T]]) {
    def toFutureTry(): Future[Try[T]] = {
      op match {
        case Success(f) => f.map(Success(_))
        case Failure(x) => Future.failed(x)
      }
    }
  }
}

class HttpClientAdapterAkkaHttp()(implicit val mat:ActorMaterializer, val timeout:Duration) {
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
