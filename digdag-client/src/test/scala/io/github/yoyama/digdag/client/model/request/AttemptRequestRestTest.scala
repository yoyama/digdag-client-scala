package io.github.yoyama.digdag.client.model.request

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}

class AttemptRequestRestTest extends FlatSpec with Matchers{
  "Instance" should "convert to json" in {
    val testData = List(
      (AttemptRequestRest(3, 44444), """{"workflowId":3,"sessionTime":44444}"""),
      (AttemptRequestRest(4, 55555, Some("retry1")), """{"workflowId":4,"sessionTime":55555,"retryAttemptName":"retry1"}"""),
      (AttemptRequestRest(5, 66666, Some("retry2"),
        params = Some(Json.obj("p1" -> 123, "p2" -> "abc"))),
        """{"workflowId":5,"sessionTime":66666,"retryAttemptName":"retry2","params":{"p1":123,"p2":"abc"}}"""),
      (AttemptRequestRest(6, 77777, Some("retry3"), resumeAttemptId = Some(666L), resumeMode = Some("failed")),
        """{"workflowId":6,"sessionTime":77777,"retryAttemptName":"retry3","resume":{"mode":"failed","attemptId":666}}"""),
    )
    testData.foreach { t =>
      assert(Json.toJson(t._1).toString() === t._2)
    }
  }
}

