package io.github.yoyama.digdag.client.model
import scala.language.postfixOps
import java.time.{Instant, OffsetDateTime}

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.Try

case class  ScheduleRest(id:String, project:IdAndName, workflow:IdAndName,
                         nextRunTime:Option[OffsetDateTime], nextScheduleTime:Option[OffsetDateTime],
                         createdAt:Option[OffsetDateTime], updatedAt:Option[OffsetDateTime], disabledAt:Option[OffsetDateTime])

object ScheduleRest extends ModelUtils {
  implicit val scheduleReads: Reads[ScheduleRest] = (
    (JsPath \ "id").read[String]
      and (JsPath \ "project").read[IdAndName]
      and (JsPath \ "workflow").read[IdAndName]
      and (JsPath \ "nextRunTime").readNullable[OffsetDateTime]
      and (JsPath \ "nextScheduleTime").readNullable[OffsetDateTime]
      and (JsPath \ "createdAt").readNullable[OffsetDateTime]
      and (JsPath \ "updatedAt").readNullable[OffsetDateTime]
      and (JsPath \ "disabledAt").readNullable[OffsetDateTime]
    ) (ScheduleRest.apply _)

  def toSchedules(response:String):Try[List[ScheduleRest]] = getAsList[ScheduleRest](response, "schedules")

  def toSchedules(jsonArray:JsArray):Try[List[ScheduleRest]] = getAsList(jsonArray)

  def toSchedule(response:String):Try[ScheduleRest] = getAsSingle(response)

  def toSchedule(json:JsValue):Try[ScheduleRest] = getAsSingle(json)

}








