package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka, HttpClientAkkaResponse}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import com.twitter.finagle.http.Response
import wvlet.airframe.http.Router
import wvlet.airframe.http.finagle.FinagleServer

import scala.util.{Failure, Success}
import scala.language.postfixOps



class ProjectApiTest  extends FlatSpec with Matchers {
  val serverPort = 55432
  val endPointURL = s"http://localhost:${serverPort}/api"
  val srvInfo = DigdagServerInfo(s"http://localhost:${serverPort}")
  val httpClient = new HttpClientAkka
  val api =  new ProjectApi(httpClient, srvInfo)

  "Simple airframe test" should "succeed" in {
    new Fixture {
      design.build[FinagleServer] { server =>
        Await.result(api.getProjects(), 60 seconds) match {
          case Success(projects) => {
            assert(projects.size == 2)
            assert(projects.head.name == "prj1")
            assert(projects.tail.head.revision == "5e8cfbd8-73d9-4de5-84e5-7cb781c82551")
          }
          case Failure(exception) => assert(false)
        }
        server.stop
      }
    }
  }


  trait Fixture {
    import wvlet.airframe.http.{Endpoint, HttpMethod}
    import wvlet.airframe.http.finagle._

    val router = Router.add[DigdagApi]

    val design = newFinagleServerDesign(port = serverPort, router = router)

    @Endpoint(path="/api")
    trait DigdagApi {
      @Endpoint(method = HttpMethod.GET, path = "/projects")
      def getPorjects(): Response = {
        val response = Response()
        response.contentString =
          """
            |{
            |  "projects": [
            |    {
            |      "id": "1",
            |      "name": "prj1",
            |      "revision": "f4924673-2e31-4582-90c6-bec8f21b4680",
            |      "createdAt": "2019-05-02T15:10:44Z",
            |      "updatedAt": "2019-05-02T15:11:31Z",
            |      "deletedAt": null,
            |      "archiveType": "db",
            |      "archiveMd5": "HxSzgWODvdFvHhCFR/nV4w=="
            |    },
            |    {
            |      "id": "2",
            |      "name": "prj2",
            |      "revision": "5e8cfbd8-73d9-4de5-84e5-7cb781c82551",
            |      "createdAt": "2019-05-02T15:12:29Z",
            |      "updatedAt": "2019-05-02T15:12:29Z",
            |      "deletedAt": null,
            |      "archiveType": "db",
            |      "archiveMd5": "pj7itvGXUqGvtK4P8lhVqQ=="
            |    }
            |  ]
            |}
            |
            |""".stripMargin
        response.contentType = "application/json;charset=utf-8"
        response
      }
    }
  }
}
