package io.github.yoyama.digdag.client.model

import java.time.OffsetDateTime

import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success, Try}

class TaskRestTest extends FlatSpec with Matchers {

  "Valid json" should "parsed" in {
    val data = """{
                 |  "tasks": [
                 |    {
                 |      "id": "1",
                 |      "fullName": "+wf1",
                 |      "parentId": null,
                 |      "config": {},
                 |      "upstreams": [],
                 |      "state": "success",
                 |      "cancelRequested": false,
                 |      "exportParams": {},
                 |      "storeParams": {},
                 |      "stateParams": {},
                 |      "updatedAt": "2019-05-02T15:11:45Z",
                 |      "retryAt": null,
                 |      "startedAt": null,
                 |      "error": {},
                 |      "isGroup": true
                 |    },
                 |    {
                 |      "id": "2",
                 |      "fullName": "+wf1+task1",
                 |      "parentId": "1",
                 |      "config": {
                 |        "echo>": "hogehoge"
                 |      },
                 |      "upstreams": [],
                 |      "state": "success",
                 |      "cancelRequested": false,
                 |      "exportParams": {},
                 |      "storeParams": {},
                 |      "stateParams": {},
                 |      "updatedAt": "2019-05-02T15:11:45Z",
                 |      "retryAt": null,
                 |      "startedAt": "2019-05-02T15:11:39Z",
                 |      "error": {},
                 |      "isGroup": false
                 |    }
                 |  ]
                 |}
                 |
    """.stripMargin
    val tasks: Try[Seq[TaskRest]] = TaskRest.toTasks(data)
    tasks match {
      case Success(a) => {
        assert(a.size == 2)
        assert(a.head.updatedAt ==  Option(OffsetDateTime.parse( "2019-05-02T15:11:45Z", SessionRest.dateTimeFormatter)))
        assert(a.head.fullName == "+wf1")
      }
      case Failure(exception) => {
        println(exception)
        assert(false)
      }
    }
  }
}
