package io.github.yoyama.digdag.client.model.request


import io.github.yoyama.digdag.client.model.{ModelUtils, SessionRest}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}

class AttemptRequestRestTest extends FlatSpec with Matchers with ModelUtils{
  "convert to json" should "work" in {
    val testData = List(
      (AttemptRequestRest(3, toInstant("2019-01-01T01:11:00Z")),
        """{"workflowId":3,"sessionTime":"2019-01-01T01:11:00Z","params":{}}"""),
      (AttemptRequestRest(4, toInstant("2019-02-02T02:22:12Z"), Some("retry1")),
        """{"workflowId":4,"sessionTime":"2019-02-02T02:22:12Z","retryAttemptName":"retry1","params":{}}"""),
      (AttemptRequestRest(5, toInstant("2019-03-13T03:33:31Z"), Some("retry2"),
        params = Json.obj("p1" -> 123, "p2" -> "abc")),
        """{"workflowId":5,"sessionTime":"2019-03-13T03:33:31Z","retryAttemptName":"retry2","params":{"p1":123,"p2":"abc"}}"""),
      (AttemptRequestRest(6, toInstant("2019-04-14T04:44:41Z"), Some("retry3"), resumeAttemptId = Some(666L), resumeMode = Some("failed")),
        """{"workflowId":6,"sessionTime":"2019-04-14T04:44:41Z","retryAttemptName":"retry3","resume":{"mode":"failed","attemptId":666},"params":{}}"""),
    )
    testData.foreach { t =>
      assert(Json.toJson(t._1).toString() === t._2)
    }
  }
}

