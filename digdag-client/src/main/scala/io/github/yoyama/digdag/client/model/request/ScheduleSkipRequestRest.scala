package io.github.yoyama.digdag.client.model.request

import java.time.{Instant, OffsetDateTime}

import play.api.libs.json.{JsObject, JsPath, JsValue, Writes}
import play.api.libs.functional.syntax._


case class ScheduleSkipRequestRest(count:Option[Long] = None, fromTime:Option[Instant] = None, nextTime:Option[OffsetDateTime] = None,
                                   nextRunTime:Option[Instant] = None, dryRun:Boolean)

object ScheduleSkipRequestRest {
  implicit val skipReqWrite: Writes[ScheduleSkipRequestRest] =  (
    (JsPath \ "count").writeNullable[Long] and
    (JsPath \ "fromTime").writeNullable[Instant] and
    (JsPath \ "nextTime").writeNullable[OffsetDateTime] and
    (JsPath \ "nextRunTime").writeNullable[Instant] and
    (JsPath \ "dryRun").write[Boolean]
  )(unlift(ScheduleSkipRequestRest.unapply))
}
