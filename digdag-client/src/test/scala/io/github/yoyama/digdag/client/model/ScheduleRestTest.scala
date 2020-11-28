package io.github.yoyama.digdag.client.model

import java.time.OffsetDateTime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class ScheduleRestTest extends AnyFlatSpec with Matchers {

  "toSchedules() with valid json" should "parsed" in {
    val data = """{
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
                 |      "nextRunTime": "2020-11-28T14:13:12Z",
                 |      "nextScheduleTime": "2020-11-15T23:50:00+09:00",
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
                 |      "nextRunTime": "2020-11-28T14:13:12Z",
                 |      "nextScheduleTime": "2020-11-20T04:42:00+09:00",
                 |      "disabledAt": null
                 |    }
                 |  ]
                 |}
    """.stripMargin
    val schedules: Try[Seq[ScheduleRest]] = ScheduleRest.toSchedules(data)
    schedules match {
      case Success(a) => {
        assert(a.size == 2)
        assert(a.head.project ==  IdAndName(Option("1"), Option("yy_schedule")))
        assert(a.head.nextRunTime.isDefined)
        assert(a.head.nextRunTime.get == OffsetDateTime.parse( "2020-11-28T14:13:12Z", ScheduleRest.dateTimeFormatter))
        assert(a.head.disabledAt == None)
      }
      case Failure(exception) => {
        println(exception)
        assert(false)
      }
    }
  }
}
