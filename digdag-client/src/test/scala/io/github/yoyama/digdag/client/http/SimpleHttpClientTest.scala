package io.github.yoyama.digdag.client.http

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS
import org.scalatest.{FlatSpec, Matchers}
import wvlet.airframe.http.finagle.FinagleServer

class SimpleHttpClientTest extends FlatSpec with Matchers {
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
