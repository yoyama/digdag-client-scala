package yoyama.digdag.client.rest.model

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class WorkflowRestTest extends FlatSpec with Matchers {

  "Valid json" should "parsed" in {
    val data = """{
                 |  "workflows": [
                 |    {
                 |      "id": "2",
                 |      "name": "wf2",
                 |      "project": {
                 |        "id": "2",
                 |        "name": "prj2"
                 |      },
                 |      "revision": "5e8cfbd8-73d9-4de5-84e5-7cb781c82551",
                 |      "timezone": "UTC",
                 |      "config": {
                 |        "+task1": {
                 |          "sh>": "echo \"hogehoge\""
                 |        },
                 |        "+task2": {
                 |          "sh>": "echo \"fugafuga\""
                 |        }
                 |      }
                 |    }
                 |  ]
                 |}
    """.stripMargin
    val workflows: Try[Seq[WorkflowRest]] = WorkflowRest.toWorkflows(data)
    println(workflows)
    assert(workflows.isSuccess)
  }

  "Invalid json1" should "be error" in {
    val data = """ { "proj" : [
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
    val workflows: Try[Seq[WorkflowRest]] = WorkflowRest.toWorkflows(data)
    println(workflows)
    assert(workflows.isFailure)
  }
}
