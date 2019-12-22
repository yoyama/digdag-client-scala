package io.github.yoyama.digdag.client


import io.github.yoyama.digdag.client.http.{SimpleHttpClient, SimpleHttpResponse}
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.concurrent.duration._

class DigdagClientTest  extends FlatSpec with Matchers with MockFactory {

  "projects" should "succeed" in {
    new Fixture {
      (httpClient.callGetString _).expects("http://localhost:65432/api/projects", *, *).returns(Future {
        new SimpleHttpResponse[String](
          status = "200 OK",
          contentType = Some("application/json"),
          contentLength = None,
          headers = Map(),
          body = Some(
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
          )
        )
      })

      val prj = client.projects()
      assert(prj.head.name == "test-proj1")
      println(prj)
    }
  }

  trait Fixture {
    implicit val timeout = 60 seconds
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val ec:ExecutionContext = global
    val srvInfo = DigdagServerInfo("http://localhost:65432")
    val httpClient = mock[SimpleHttpClient]
    val client = DigdagClient(httpClient, srvInfo)

  }
}

