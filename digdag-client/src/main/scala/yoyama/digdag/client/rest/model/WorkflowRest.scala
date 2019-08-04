package yoyama.digdag.client.rest.model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

case class  WorkflowRest(id:String, name:String, projectId:String, projectName:String,
                        revision:String, timezone:String, config:String)

object WorkflowRest extends ModelUtils {

  implicit val workflowReads: Reads[WorkflowRest] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "project" \ "id").read[String] and
      (JsPath \ "project" \ "name").read[String] and
      (JsPath \ "revision").read[String] and
      (JsPath \ "timezone").read[String]
      and jsonStringRead
    ) (WorkflowRest.apply _)

  def toWorkflows(response:String):Try[List[WorkflowRest]] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      lists: JsArray <- toJsArray(parsed("workflows"))
      projects <- toWorkflows(lists)
    } yield projects
  }

  def toWorkflows(jsonArray:JsArray):Try[List[WorkflowRest]] = {
    jsonArray.validate[List[WorkflowRest]] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }
}






