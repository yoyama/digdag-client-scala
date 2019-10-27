package io.github.yoyama.digdag.client.model

import scala.language.postfixOps
import java.time.OffsetDateTime

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.Try


case class AttemptRest(id: String, index:Long,  project:IdAndName, workflow:IdAndName,
                       sessionId:String, sessionUuid:String, sessionTime:OffsetDateTime,
                       retryAttemptName: Option[String], done: Boolean, success: Boolean,
                       cancelRequested:Boolean, params:JsValue,
                       createdAt:OffsetDateTime, finishedAt:Option[OffsetDateTime])

object AttemptRest extends ModelUtils {
  implicit val attemptReads: Reads[AttemptRest] = (
          (JsPath \ "id").read[String]
      and (JsPath \ "index").read[Long]
      and (JsPath \ "project").read[IdAndName]
      and (JsPath \ "workflow").read[IdAndName]
      and (JsPath \ "sessionId").read[String]
      and (JsPath \ "sessionUuid").read[String]
      and (JsPath \ "sessionTime").read[OffsetDateTime]
      and (JsPath \ "retryAttemptName").readNullable[String]
      and (JsPath \ "done").read[Boolean]
      and (JsPath \ "success").read[Boolean]
      and (JsPath \ "cancelRequested").read[Boolean]
      and (JsPath \ "params").read[JsValue]
      and (JsPath \ "createdAt").read[OffsetDateTime]
      and (JsPath \ "finishedAt").readNullable[OffsetDateTime]
    )(AttemptRest.apply _)

  def toAttempts(response:String):Try[List[AttemptRest]] = getAsList[AttemptRest](response, "attempts")

  def toAttempts(jsonArray:JsArray):Try[List[AttemptRest]] = getAsList(jsonArray)

  def toAttempt(response:String):Try[AttemptRest] = getAsSingle(response)

  def toAttempt(json:JsValue):Try[AttemptRest] = getAsSingle(json)

}
