package io.github.yoyama.digdag.client.model

import java.time.OffsetDateTime

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.postfixOps
import scala.util.Try


case class TaskRest(id:String, fullName:String,  parentId:Option[String], config:JsValue,
                    upstreams:Seq[String], state:String, cancelRequested:Boolean,
                    exportParams:JsValue, storeParams:JsValue, stateParams:JsValue,
                    updatedAt:Option[OffsetDateTime], retryAt:Option[OffsetDateTime],
                    startedAt:Option[OffsetDateTime], error:JsValue, isGroup:Boolean)


object TaskRest extends ModelUtils {
  implicit val taskReads: Reads[TaskRest] = (
          (JsPath \ "id").read[String]
      and (JsPath \ "fullName").read[String]
      and (JsPath \ "parentId").readNullable[String]
      and (JsPath \ "config").read[JsValue]
      and (JsPath \ "upstreams").read[Seq[String]]
      and (JsPath \ "state").read[String]
      and (JsPath \ "cancelRequested").read[Boolean]
      and (JsPath \ "exportParams").read[JsValue]
      and (JsPath \ "storeParams").read[JsValue]
      and (JsPath \ "stateParams").read[JsValue]
      and (JsPath \ "updatedAt").readNullable[OffsetDateTime]
      and (JsPath \ "retryAt").readNullable[OffsetDateTime]
      and (JsPath \ "startedAt").readNullable[OffsetDateTime]
      and (JsPath \ "error").read[JsValue]
      and (JsPath \ "isGroup").read[Boolean]
    )(TaskRest.apply _)

  def toTasks(response:String):Try[List[TaskRest]] = getAsList[TaskRest](response, "tasks")

  def toTasks(jsonArray:JsArray):Try[List[TaskRest]] = getAsList(jsonArray)

  def toTask(response:String):Try[TaskRest] = getAsSingle(response)

  def toTask(json:JsValue):Try[TaskRest] = getAsSingle(json)

}
