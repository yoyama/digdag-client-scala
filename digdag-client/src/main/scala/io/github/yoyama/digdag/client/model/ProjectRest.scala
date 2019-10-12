package io.github.yoyama.digdag.client.model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

case class ProjectRest(id:String, name:String, revision:String, createdAt:String, updatedAt:Option[String],
                       deletedAt:Option[String], archiveType:Option[String], archiveMd5:Option[String]);

object ProjectRest extends ModelUtils {
  implicit val projectReads: Reads[ProjectRest] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "revision").read[String] and
      (JsPath \ "createdAt").read[String] and
      (JsPath \ "updatedAt").readNullable[String] and
      (JsPath \ "deletedAt").readNullable[String] and
      (JsPath \ "archiveType").readNullable[String] and
      (JsPath \ "archiveMd5").readNullable[String]
    )(ProjectRest.apply _)

  def toProjects(response:String):Try[List[ProjectRest]] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      lists: JsArray <- toJsArray(parsed("projects"))
      projects <- toProjects(lists)
    } yield projects
  }

  def toProjects(jsonArray:JsArray):Try[List[ProjectRest]] = {
    jsonArray.validate[List[ProjectRest]] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

  def toProject(response:String):Try[ProjectRest] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      project <- toProject(parsed)
    } yield project
  }


  def toProject(json:JsValue):Try[ProjectRest] = {
    json.validate[ProjectRest] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }
}
