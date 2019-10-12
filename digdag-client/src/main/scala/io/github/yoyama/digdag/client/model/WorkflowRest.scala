package io.github.yoyama.digdag.client.model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

case class  WorkflowRest(id:String, name:String, projectId:String, projectName:String,
                        revision:String, timezone:String, config:JsValue)

object WorkflowRest extends ModelUtils {

  implicit val workflowReads: Reads[WorkflowRest] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "project" \ "id").read[String] and
      (JsPath \ "project" \ "name").read[String] and
      (JsPath \ "revision").read[String] and
      (JsPath \ "timezone").read[String]
      and
      (JsPath \ "config").read[JsValue]
      //and jsonStringRead
    ) (WorkflowRest.apply _)

  def toWorkflows(response:String):Try[List[WorkflowRest]] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      lists: JsArray <- toJsArray(parsed("workflows"))
      workflows <- toWorkflows(lists)
    } yield workflows
  }

  def toWorkflows(jsonArray:JsArray):Try[List[WorkflowRest]] = {
    jsonArray.validate[List[WorkflowRest]] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

  def toWorkflow(response:String):Try[WorkflowRest] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      workflow <- toWorkflow(parsed)
    } yield workflow
  }


  def toWorkflow(json:JsValue):Try[WorkflowRest] = {
    println(json.toString())
    json.validate[WorkflowRest] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

}






