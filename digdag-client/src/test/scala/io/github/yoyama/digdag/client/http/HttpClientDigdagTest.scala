package io.github.yoyama.digdag.client.http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType => AkkaContentType, HttpEntity => AkkaHttpEntity, HttpMessage => AkkaHttpMessage, HttpMethod => AkkaHttpMethod, HttpMethods => AkkaHttpMethods, HttpRequest => AkkaHttpRequest, HttpResponse => AkkaHttpResponse, MediaTypes => AkkaMediaTypes, Uri => AkkaUri}
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}
import wvlet.airframe.http.HttpMethod
import wvlet.airframe.http.finagle.FinagleServer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class HttpClientDigdagTest extends FlatSpec with Matchers {
  implicit val timeout = 60 seconds
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val adapters = new HttpClientAdapters4AkkaHttp
  val ah = adapters.AkkaHttpHelper

  "convertEntity" should "work" in {
    assert(ah.convertEntity[String](AkkaHttpEntity("test body")) == "test body")
    assert(ah.convertEntity[String](AkkaHttpEntity.Empty) == "")
  }

  "contentType" should "work" in {
    val req1 = AkkaHttpRequest(AkkaHttpMethods.GET, AkkaUri("http://localhost/"),
                          entity = AkkaHttpEntity.Empty.withContentType(AkkaContentType(AkkaMediaTypes.`application/json`)))
    assert(ah.contentType(req1) == Some("application/json"))

    val req2 = AkkaHttpRequest(AkkaHttpMethods.GET, AkkaUri("http://localhost/"),
      entity = AkkaHttpEntity.Empty)
    assert(ah.contentType(req2) == None)
  }

  "method" should "work" in {
    assert(ah.method(AkkaHttpMethods.GET) == HttpMethod.GET)
    assert(ah.method(AkkaHttpMethods.POST) == HttpMethod.POST)
    assert(ah.method(AkkaHttpMethods.PUT) == HttpMethod.PUT)
    assert(ah.method(AkkaHttpMethods.DELETE) == HttpMethod.DELETE)
  }

  "callGet" should "work" in {
    new HttpServerFixture {
      design.build[FinagleServer] { server =>
        val client = new HttpClientAkkaHttp()
        val responseF = client.callGet(s"http://localhost:${serverPort}/api/projects")
        responseF.onComplete( _ match {
          case Success(v) => {
            println(v.status)
            println(v.contentType)
            println(v.contentString)
            assert(v.status.code == 200)
            assert(v.contentType.isDefined)
            assert(v.contentType.get == "application/json")
            assert(v.contentString.contains("projects"))
          }
          case Failure(ex) => assert(false)
        })
        val response = Await.result(responseF, 60 seconds)
      }
    }
  }

  "contentType2" should "work" in {
    val ct = AkkaContentType.parse("application/octet-stream")
    assert(ct.isRight)
    assert("application/octet-stream" == ct.right.get.value)
  }
}
