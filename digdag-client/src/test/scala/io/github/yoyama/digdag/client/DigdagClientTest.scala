package io.github.yoyama.digdag.client

import java.net.URI

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DigdagClientTest  extends FlatSpec with Matchers with MockFactory {

  "projects" should "succeed" in {
    new Fixture {
      (httpAkka.callGet _).expects("http://localhost:65432/api/projects", *, *).returns(Future {
        new HttpClientAkkaResponse(null)(materializer){
          override def asString(): Future[String] = Future{
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
          }
        }
      })
      val prj = client.projects()
      println(prj)
    }
  }

  trait Fixture {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val srvInfo = DigdagServerInfo(new URI("http://localhost:65432"))
    val httpAkka = mock[HttpClientAkka]
    val client = DigdagClient(httpAkka, srvInfo)

  }
}
