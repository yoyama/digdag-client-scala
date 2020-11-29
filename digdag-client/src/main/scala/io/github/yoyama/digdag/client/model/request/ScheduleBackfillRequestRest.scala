package io.github.yoyama.digdag.client.model.request

import java.time.Instant

import play.api.libs.json.{JsObject, JsPath, JsValue, Writes}
import play.api.libs.functional.syntax._

case class ScheduleBackfillRequestRest(fromTime:Instant, dryRun:Boolean, attemptName:String, count:Option[Long])

object ScheduleBackfillRequestRest {
  implicit val backfillReqWrite: Writes[ScheduleBackfillRequestRest] =  (
    (JsPath \ "fromTime").write[Instant] and
      (JsPath \ "dryRun").write[Boolean] and
      (JsPath \ "attemptName").write[String] and
      (JsPath \ "count").writeNullable[Long]
    )(unlift(ScheduleBackfillRequestRest.unapply))
}
