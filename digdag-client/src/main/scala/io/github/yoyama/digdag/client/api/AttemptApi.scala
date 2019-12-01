package io.github.yoyama.digdag.client.api

import java.time.Instant

import io.github.yoyama.digdag.client.model.{AttemptRest, TaskRest}
import io.github.yoyama.digdag.client.DigdagServerInfo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import io.github.yoyama.digdag.client.commons.Helpers.HttpClientDigdagHelper
import io.github.yoyama.digdag.client.http.HttpClientAkkaHttp
import io.github.yoyama.digdag.client.model.request.AttemptRequestRest
import play.api.libs.json.{JsObject, Json}
import io.github.yoyama.digdag.client.commons.Helpers.HttpResponseHelper

class AttemptApi(httpClient: HttpClientAkkaHttp, srvInfo:DigdagServerInfo){

  val apiPathPart = "/api/attempts"
  def getAttempts(prjName:String, wfName:String, includeRetried:Boolean = false, lastId:Option[Long] = None,
                  pageSize:Option[Long] = None):Future[List[AttemptRest]] = {
    val queriesPart: Map[String, String] =
      Seq(("last_id", lastId), ("page_size", pageSize))
        .filter(_._2.isDefined)            //remove None
        .map(x => (x._1, x._2.toString))   //convert Long to String
        .toMap                             //Map[String,String]
    val queries = queriesPart + (("include_retried", includeRetried.toString))
    val apiPath = srvInfo.endPoint.toString + apiPathPart
    httpClient.callGetToRest(apiPath, queries, AttemptRest.toAttempts _)
  }

  def getAttempt(id:Long):Future[AttemptRest] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}"
    httpClient.callGetToRest(apiPath, Map.empty, AttemptRest.toAttempt _)
  }

  def getAttemptRetries(id:Long):Future[List[AttemptRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/retries"
    httpClient.callGetToRest(apiPath, Map.empty, AttemptRest.toAttempts _)
  }

  def getTasks(id:Long):Future[List[TaskRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/tasks"
    httpClient.callGetToRest(apiPath, Map.empty, TaskRest.toTasks _)
  }

  def startAttempt(workflowId:Long, sessionTime:Instant, retryAttemptName:Option[String] = None,
                   resumeAttemptId:Option[Long] = None, resumeMode:Option[String] = None,
                   paramsJson:Option[String] = None): Future[AttemptRest] = {
    val params = paramsJson.map(Json.toJson(_)).getOrElse(JsObject.empty)
    val areq = AttemptRequestRest(workflowId, sessionTime, retryAttemptName, resumeAttemptId, resumeMode, params)
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}"
    val resp = httpClient.callPut(apiPath, "application/json", Json.toJson(areq).toString())
    resp.flatMap(_.toRest(AttemptRest.toAttempt _))
  }
}

