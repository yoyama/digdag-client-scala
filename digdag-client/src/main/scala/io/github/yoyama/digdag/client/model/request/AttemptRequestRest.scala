package io.github.yoyama.digdag.client.model.request

import java.time.Instant

import play.api.libs.json.{JsObject, JsPath, JsValue, Writes}
import play.api.libs.functional.syntax._

case class AttemptRequestRest(workflowId:Long, sessionTime:Instant, retryAttemptName:Option[String] = None,
                              resumeAttemptId:Option[Long] = None, resumeMode:Option[String] = None, params:JsValue = JsObject.empty)

object AttemptRequestRest {
  implicit val attemptReqWrite: Writes[AttemptRequestRest] =  (
    (JsPath \ "workflowId").write[Long] and
      (JsPath \ "sessionTime").write[Instant] and
      (JsPath \ "retryAttemptName").writeNullable[String] and
      (JsPath \ "resume" \ "attemptId").writeNullable[Long] and
      (JsPath \ "resume" \ "mode").writeNullable[String] and
      (JsPath \ "params").write[JsValue]
    )(unlift(AttemptRequestRest.unapply))
}
