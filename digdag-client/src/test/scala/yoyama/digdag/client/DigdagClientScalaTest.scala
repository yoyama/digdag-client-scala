package yoyama.digdag.client

import java.net.{URI, URL}

import org.scalatest._

class DigdagClientScalaTest  extends FlatSpec with Matchers {
  val srvInfo = DigdagServerInfo(new URI("http://localhost:65432"))
  val client = DigdagClient(srvInfo)

  "projects" should "succeed" in {
    val prj = client.getProjects()
    println(prj)
  }
}
