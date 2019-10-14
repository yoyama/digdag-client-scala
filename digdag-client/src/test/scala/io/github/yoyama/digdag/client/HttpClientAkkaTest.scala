package io.github.yoyama.digdag.client

import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps


class HttpClientAkkaTest extends FlatSpec with Matchers {

  val httpClient = new HttpClientAkka

  "callGet http://google.com" should "succeed" in {
    val resp = httpClient.callGet("https://www.google.com/")
    println(Await.result(resp, 5 seconds))
  }

}
