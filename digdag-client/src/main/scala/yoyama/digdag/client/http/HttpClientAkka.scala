package yoyama.digdag.client.http

import akka.actor.ActorSystem
import akka.http.javadsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class HttpClientAkkaResponse(val res: HttpResponse)(implicit val mat:ActorMaterializer) {
  def asString(): Future[String] = {
    Unmarshal(res.entity).to[String]
  }
}

class HttpClientAkka {
  implicit val timeout = Timeout(60 seconds)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val convString =  (res:HttpResponse) => Unmarshal(res.entity).to[String]
  //val convJSON =  (res:HttpResponse) => Unmarshal(res.entity).to[String]

  def callGet(uri: String, queries: Map[String, String] = Map(),
              headers: Map[String, String] = Map()) : Future[HttpClientAkkaResponse] = httpRequest(GET, uri, queries, headers)

  def httpRequest(webMethod: HttpMethod, uri: String, queries:Map[String, String], headers: Map[String, String], requestBody: String = ""):Future[HttpClientAkkaResponse] = {

    val rawHeaders = headers.map {
      case (k, v) => {
        RawHeader(k, v)
      }
    }.toList

    val httpEntity = requestBody match {
      case "" => HttpEntity.Empty
      case _ => HttpEntity(requestBody)
    }
    val req = HttpRequest(webMethod, uri = Uri(uri).withQuery(Query(queries)), entity = httpEntity)
      .withHeaders(
        rawHeaders: _*,
      )
    val responseFuture: Future[HttpResponse] = Http().singleRequest(req)
    responseFuture.map(r => new HttpClientAkkaResponse(r))
  }
}
