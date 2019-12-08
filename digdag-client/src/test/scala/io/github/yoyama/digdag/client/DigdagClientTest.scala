package io.github.yoyama.digdag.client


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.github.yoyama.digdag.client.http.{HttpClientAdapters4AkkaHttp, HttpClientAkkaHttp}
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import wvlet.airframe.http.HttpResponse
import wvlet.log.LogSupport

import scala.concurrent.Future
import akka.http.scaladsl.model.{ContentType => AkkaContentType, ContentTypes => AkkaContentTypes, HttpEntity => AkkaHttpEntity, HttpMessage => AkkaHttpMessage, HttpMethod => AkkaHttpMethod, HttpMethods => AkkaHttpMethods, HttpRequest => AkkaHttpRequest, HttpResponse => AkkaHttpResponse, MediaTypes => AkkaMediaTypes, Uri => AkkaUri}

import scala.language.postfixOps
import scala.concurrent.duration._

class DigdagClientTest  extends FlatSpec with Matchers with MockFactory {

  "projects" should "succeed" in {
    new Fixture {
      import adapters._
      (httpClient.callGet _).expects("http://localhost:65432/api/projects", *, *).returns(Future {
        new HttpResponse[AkkaHttpResponse] {
          override def adapter = HttpResponseAkkaHttpAdapter
          override def toRaw: AkkaHttpResponse = null
          override def contentString: String =
            """ { "projects" : [
            { "id":"1",
              "name":"test-proj1",
              "revision":"47b78f24-7195-458c-b91a-b3fd059aba2e",
              "createdAt":"2019-04-30T14:24:38Z",
              "updatedAt":"2019-04-30T14:24:38Z",
              "deletedAt":null,
              "archiveType":"db",
              "archiveMd5":"cCkGbCesb17xjWYNV0GXmg=="
            } ] }
            """.stripMargin

        }
      })
      val prj = client.projects()
      println(prj)
    }
  }

  trait Fixture {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val timeout = 60 seconds
    import scala.concurrent.ExecutionContext.Implicits.global

    val adapters = new HttpClientAdapters4AkkaHttp()
    val srvInfo = DigdagServerInfo("http://localhost:65432")
    val httpClient = mock[HttpClientAkkaHttp]
    val client = DigdagClient(httpClient, srvInfo)

  }
}

