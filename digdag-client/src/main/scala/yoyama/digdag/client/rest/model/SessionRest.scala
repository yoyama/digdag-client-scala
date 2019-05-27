package yoyama.digdag.client.rest.model

import java.time.OffsetDateTime

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

case class SessionRest(id:String, project:IdAndName, workflow:IdAndName, uuid:String, sessionTime:OffsetDateTime)

object SessionRest extends ModelUtils {
  implicit val sessionReads: Reads[SessionRest] = (
    (JsPath \ "id").read[String]
      and (JsPath \ "project").read[IdAndName]
      and (JsPath \ "workflow").read[IdAndName]
      and (JsPath \ "sessionUuid").read[String]
      and (JsPath \ "sessionTime").read[OffsetDateTime]
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
