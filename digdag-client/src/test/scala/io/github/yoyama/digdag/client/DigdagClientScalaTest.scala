package io.github.yoyama.digdag.client

import java.net.URI

import org.scalatest._

class DigdagClientScalaTest  extends FlatSpec with Matchers {
  val srvInfo = DigdagServerInfo(new URI("http://localhost:65432"))
  val client = DigdagClient(srvInfo)

  "projects" should "succeed" in {
    val prj = client.projects()
    println(prj)
  }
}
