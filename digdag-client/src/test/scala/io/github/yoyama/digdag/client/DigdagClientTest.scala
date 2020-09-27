package io.github.yoyama.digdag.client


import io.github.yoyama.digdag.client.http.{SimpleHttpClient, SimpleHttpResponse}
import org.scalatest.{FlatSpec}
import org.mockito.Mockito._
import org.mockito.Matchers.{any,eq => eqTo}

import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.concurrent.duration._

class DigdagClientTest  extends FlatSpec with LogSupport {

  "version" should "succeed" in {
    new Fixture {
      when(httpClient.callGetString(
        eqTo("http://localhost:65432/api/version"),
        any(classOf[Map[String,String]]),
        any(classOf[Map[String,String]]))
      ).thenReturn(Future {
        new SimpleHttpResponse[String](
          status = "200 OK",
          contentType = Some("application/json"),
          contentLength = None,
          headers = Map(),
          body = Some(
            """{"version":"0.9.42"}
            """.stripMargin
          )
        )
      })

      val version = client.version()
      println(version)
      assert(version.get == "0.9.42")
    }
  }

  "projects" should "succeed" in {
    new Fixture {
      when(httpClient.callGetString(
        eqTo("http://localhost:65432/api/projects"),
        any(classOf[Map[String,String]]),
        any(classOf[Map[String,String]]))
      ).thenReturn(Future {
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
      assert(prj.isSuccess)
      assert(prj.get.head.name == "test-proj1")
      println(prj)
    }
  }

  trait Fixture {
    implicit val timeout = 60 seconds
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val ec:ExecutionContext = global
    val srvInfo = ConnectionConfig("test", "http://localhost:65432")
    val httpClient = mock(classOf[SimpleHttpClient])
    val client = DigdagClient(httpClient, srvInfo)
  }
}

