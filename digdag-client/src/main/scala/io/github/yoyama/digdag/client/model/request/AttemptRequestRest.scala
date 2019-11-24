package io.github.yoyama.digdag.client.model.request

import play.api.libs.json.{JsObject, JsPath, JsValue, Writes}
import play.api.libs.functional.syntax._

case class AttemptRequestRest(workflowId:Long, sessionTime:Long, retryAttemptName:Option[String] = None,
                              resumeAttemptId:Option[Long] = None, resumeMode:Option[String] = None, params:Option[JsValue] = None)

object AttemptRequestRest {
  implicit val attemptReqWrite: Writes[AttemptRequestRest] =  (
    (JsPath \ "workflowId").write[Long] and
      (JsPath \ "sessionTime").write[Long] and
      (JsPath \ "retryAttemptName").writeNullable[String] and
      (JsPath \ "resume" \ "attemptId").writeNullable[Long] and
      (JsPath \ "resume" \ "mode").writeNullable[String] and
      (JsPath \ "params").writeNullable[JsValue]
    )(unlift(AttemptRequestRest.unapply))
}
