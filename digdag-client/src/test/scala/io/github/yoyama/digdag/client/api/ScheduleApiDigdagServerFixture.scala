package io.github.yoyama.digdag.client.api

import scala.concurrent.ExecutionContext.Implicits.global
import com.twitter.finagle.http.Response
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

trait ScheduleApiDigdagServerFixture extends ApiDigdagServerFixture {
  override def serverPort = 55532
  override def router = Router.add[ScheduleApiServer]
  val scheduleApi = new ScheduleApi(httpClient, connInfo)

  @Endpoint(path = "/api")
  trait ScheduleApiServer {
    @Endpoint(method = HttpMethod.GET, path = "/schedules")
    def getSchedules(): Response = {
      val response = Response()
      response.contentString =
        s"""
           |{
           |  "schedules": [
           |    {
           |      "id": "1",
           |      "project": {
           |        "id": "1",
           |        "name": "yy_schedule"
           |      },
           |      "workflow": {
           |        "id": "1",
           |        "name": "sch2"
           |      },
           |      "nextRunTime": "2020-11-29T13:06:39Z",
           |      "nextScheduleTime": "2020-11-17T01:50:00+09:00",
           |      "disabledAt": null
           |    },
           |    {
           |      "id": "2",
           |      "project": {
           |        "id": "1",
           |        "name": "yy_schedule"
           |      },
           |      "workflow": {
           |        "id": "2",
           |        "name": "sch1"
           |      },
           |      "nextRunTime": "2020-11-29T13:06:39Z",
           |      "nextScheduleTime": "2020-11-22T01:28:00+09:00",
           |      "disabledAt": null
           |    }
           |  ]
           |}
           |""".stripMargin
      response.contentType = "application/json;charset=utf-8"
      response
    }
  }
}
