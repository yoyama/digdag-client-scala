package io.github.yoyama.digdag.client.model

import scala.language.postfixOps
import java.time.{Instant, OffsetDateTime}

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

case class SessionRest(id: String, project: IdAndName, workflow: IdAndName, uuid: String,
                sessionTime: Instant, lastAttempt: Option[SessionRest.LastAttempt])

object SessionRest extends ModelUtils {
  case class LastAttempt(id: String, retryAttemptName: Option[String], done: Boolean, success: Boolean,
                         cancelRequested: Boolean, params: JsValue, createdAt:String, finishedAt:Option[String])

  implicit val lastAttemptReads: Reads[LastAttempt] = (
    (JsPath \ "id").read[String]
      and (JsPath \ "retryAttemptName").readNullable[String]
      and (JsPath \ "done").read[Boolean]
      and (JsPath \ "success").read[Boolean]
      and (JsPath \ "cancelRequested").read[Boolean]
      and (JsPath \ "params").read[JsValue]
      and (JsPath \ "createdAt").read[String]
      and (JsPath \ "finishedAt").readNullable[String]
    )(LastAttempt.apply _)

  implicit val sessionReads: Reads[SessionRest] = (
    (JsPath \ "id").read[String]
      and (JsPath \ "project").read[IdAndName]
      and (JsPath \ "workflow").read[IdAndName]
      and (JsPath \ "sessionUuid").read[String]
      and (JsPath \ "sessionTime").read[Instant]
      and (JsPath \ "lastAttempt").readNullable[LastAttempt]
    )(SessionRest.apply _)

  def toSessions(response:String):Try[List[SessionRest]] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      lists: JsArray <- toJsArray(parsed("sessions"))
      sessions <- toSessions(lists)
    } yield sessions
  }

  def toSessions(jsonArray:JsArray):Try[List[SessionRest]] = {
    jsonArray.validate[List[SessionRest]] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

  def toSession(response:String):Try[SessionRest] = {
    for {
      parsed:JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      session <- toSession(parsed)
    } yield session
  }

  def toSession(json:JsValue):Try[SessionRest] = {
    json.validate[SessionRest] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }
}

