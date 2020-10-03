package io.github.yoyama.digdag.client.api

import scala.concurrent.ExecutionContext.Implicits.global

import com.twitter.finagle.http.Response
import io.github.yoyama.digdag.client.ConnectionConfig
import io.github.yoyama.digdag.client.http.SimpleHttpClientScalaJ
import wvlet.airframe.Design
import wvlet.airframe.http.Router

private[api] trait ApiDigdagServerMockFixture {

  import wvlet.airframe.http.finagle._
  import wvlet.airframe.http.{Endpoint, HttpMethod}

  val serverPort = 55432
  val endPointURL = s"http://localhost:${serverPort}/api"
  val connInfo = ConnectionConfig("test", s"http://localhost:${serverPort}")
  val httpClient = new SimpleHttpClientScalaJ()
  val api = new ProjectApi(httpClient, connInfo)

  val router = Router.add[DigdagApi]

  val finagleDesign: Design = newFinagleServerDesign(port = serverPort, router = router)

  @Endpoint(path = "/api")
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
