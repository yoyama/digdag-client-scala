package yoyama.digdag.client.http

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.functional.syntax._

class HttpClientAkkaTest extends FlatSpec with Matchers {

  val httpClient = new HttpClientAkka

  "callGet http://google.com" should "succeed" in {
    val resp = httpClient.callGet("https://www.google.com/")
    println(Await.result(resp, 5 seconds))
  }

}
