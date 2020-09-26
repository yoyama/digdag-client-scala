package io.github.yoyama.digdag.client.http

import java.nio.file.{Files, Path, Paths}

import wvlet.airspec._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS
import org.mockito.Mockito._
import org.mockito.Matchers.{any, eq => eqTo}
import wvlet.airframe.http.finagle.FinagleServer

import scala.concurrent.ExecutionContext.Implicits.global

class SimpleHttpClientTest extends AirSpec {
  def testCallGetString: Unit = {
    new DigdagServerMockFixture {
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

  /**
  def testCallGetDownload: Unit = {
    //ToDo collect implementation
    new DigdagServerMockFixture {
      design.withSession { ss =>
        val hc = new SimpleHttpClientScalaJ
        val resp = hc.callGetDownload("http://localhost:9797/api/projects/1/archive", out = Paths.get("/tmp/download1.dat"))
        val ret = Await.result(resp, Duration(30, SECONDS))
        println(ret)
        println(ret.body)
        println(ret.contentType)
        println(ret.contentLength)
        assert(true)
      }
    }
  }
    */

  /**
  def testCallPutUpload: Unit = {
    //ToDo collect implementation
    val tempDir = Files.createTempDirectory("testcallPutUpload-")
    val uploadFile = Files.createFile( tempDir.resolve("upload1.dat"))
    val hc = new SimpleHttpClientScalaJ
    val resp = hc.callPutUpload(
      uri = "http://localhost:65432/api/projects",
      contentType = "application/gzip",
      contentPath = uploadFile,
      queries = Map("project" -> "hoge", "revision" -> "aaaa")
    )
    val ret = Await.result(resp, Duration(30, SECONDS))
    println(ret)
    println(ret.body)
    println(ret.contentType)
    println(ret.contentLength)
    assert(true)
  }
    */
}
