package io.github.yoyama.digdag.client.model

import java.time.OffsetDateTime


import scala.util.{Failure, Success, Try}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AttemptRestTest extends AnyFlatSpec with Matchers {

  "Valid json" should "parsed" in {
    val data = """{
                 |  "attempts": [
                 |    {
                 |      "id": "7",
                 |      "index": 1,
                 |      "project": {
                 |        "id": "2",
                 |        "name": "prj2"
                 |      },
                 |      "workflow": {
                 |        "name": "wf2",
                 |        "id": "2"
                 |      },
                 |      "sessionId": "6",
                 |      "sessionUuid": "4eb3c965-c7fe-46a8-87ff-75e3c97dd976",
                 |      "sessionTime": "2019-05-02T15:12:38+00:00",
                 |      "retryAttemptName": null,
                 |      "done": true,
                 |      "success": true,
                 |      "cancelRequested": false,
                 |      "params": {},
                 |      "createdAt": "2019-05-02T15:12:38Z",
                 |      "finishedAt": "2019-05-02T15:12:41Z"
                 |    },
                 |    {
                 |      "id": "5",
                 |      "index": 1,
                 |      "project": {
                 |        "id": "2",
                 |        "name": "prj2"
                 |      },
                 |      "workflow": {
                 |        "name": "wf2",
                 |        "id": "2"
                 |      },
                 |      "sessionId": "5",
                 |      "sessionUuid": "293e34f9-a593-4591-b563-63c51bca28b0",
                 |      "sessionTime": "2019-05-02T15:12:37+00:00",
                 |      "retryAttemptName": null,
                 |      "done": true,
                 |      "success": true,
                 |      "cancelRequested": false,
                 |      "params": {},
                 |      "createdAt": "2019-05-02T15:12:37Z",
                 |      "finishedAt": "2019-05-02T15:12:41Z"
                 |    }
                 |  ]
                 |}
    """.stripMargin
    val attempts: Try[Seq[AttemptRest]] = AttemptRest.toAttempts(data)
    attempts match {
      case Success(a) => {
        assert(a.size == 2)
        assert(a.head.sessionTime ==  OffsetDateTime.parse( "2019-05-02T15:12:38+00:00", AttemptRest.dateTimeFormatter).toInstant)
        assert(a.head.retryAttemptName.isEmpty)
      }
      case Failure(exception) => {
        println(exception)
        assert(false)
      }
    }
  }
}
