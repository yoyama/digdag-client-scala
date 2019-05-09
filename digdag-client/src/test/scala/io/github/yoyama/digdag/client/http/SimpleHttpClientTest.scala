package io.github.yoyama.digdag.client.http

import java.nio.file.{Files, Path, Paths}

import wvlet.airspec._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS

class SimpleHttpClientTest extends AirSpec {
  def testCallGetString: Unit = {
    val hc = new SimpleHttpClientScalaJ
    val resp = hc.callGetString("http://yahoo.co.jp/")
    val ret = Await.result(resp, Duration(30, SECONDS))
    println(ret)
    println(ret.body)
    println(ret.contentType)
    println(ret.contentLength)
    assert(true)
  }

  def testCallGetDownload: Unit = {
    //ToDo collect implementation
    val hc = new SimpleHttpClientScalaJ
    val resp = hc.callGetDownload("http://yahoo.co.jp/", out = Paths.get("/tmp/download1.dat"))
    val ret = Await.result(resp, Duration(30, SECONDS))
    println(ret)
    println(ret.body)
    println(ret.contentType)
    println(ret.contentLength)
    assert(true)
  }

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

}
