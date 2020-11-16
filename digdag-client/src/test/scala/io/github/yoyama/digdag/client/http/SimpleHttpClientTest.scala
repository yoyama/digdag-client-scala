package io.github.yoyama.digdag.client.http

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS
import wvlet.airframe.http.finagle.FinagleServer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimpleHttpClientTest extends AnyFlatSpec with Matchers {
  "statusCode" should "be parsed" in {
    val resp = SimpleHttpResponse(status = "HTTP1.1 202 Accepted", contentType = Option("application/json") ,
                contentLength = None, headers = Map.empty, body = Option("dummy"))
    resp.statusCode match {
      case Some(202) => //OK
      case Some(v) => fail(s"Invalid status code: ${v}")
      case None => fail(s"Cannot get status code: ${resp.statusCode}")
    }
  }

  "callGetString" should "work" in {
    new HttpDigdagServerMockFixture {
      design.withSession { ss =>
        val server: FinagleServer = ss.build[FinagleServer]
        val hc = new SimpleHttpClientScalaJ
        val resp = hc.callGetString("http://localhost:9797/api/projects")
        val ret = Await.result(resp, Duration(30, SECONDS))
        assert(ret.body.isDefined)
        assert(ret.body.get.contains("HxSzgWODvdFvHhCFR/nV4w=="))
        assert(Some("application/json;charset=utf-8") == ret.contentType)
        assert(Some(299) == ret.contentLength)
      }
    }
  }
}
