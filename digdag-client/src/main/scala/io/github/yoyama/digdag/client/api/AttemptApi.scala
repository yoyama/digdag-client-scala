package io.github.yoyama.digdag.client.api

import java.time.Instant

import io.github.yoyama.digdag.client.model.{AttemptRest, TaskRest}
import io.github.yoyama.digdag.client.DigdagServerInfo

import scala.concurrent.{ExecutionContext, Future}
import io.github.yoyama.digdag.client.http.{SimpleHttpClient, SimpleHttpResponse}
import io.github.yoyama.digdag.client.model.request.AttemptRequestRest
import play.api.libs.json.{JsObject, Json}
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, SimpleHttpClientHelper, TryHelper}
import wvlet.log.LogSupport

class AttemptApi(httpClient: SimpleHttpClient, srvInfo:DigdagServerInfo)(implicit val ec:ExecutionContext) extends LogSupport {
  val apiPathPart = "/api/attempts"
  def getAttempts(prjName:String, wfName:String, includeRetried:Boolean = false, lastId:Option[Long] = None,
                  pageSize:Option[Long] = None):Future[List[AttemptRest]] = {
    def procQueries():Map[String,String] = {
      val queriesPart: Map[String, String] =
        Seq(("last_id", lastId), ("page_size", pageSize))
          .filter(_._2.isDefined)            //remove None
          .map(x => (x._1, x._2.toString))   //convert Long to String
          .toMap                             //Map[String,String]
      queriesPart + (("include_retried", includeRetried.toString))
    }
    def queries: Map[String,String] = procQueries()
    val apiPath = srvInfo.endPoint.toString + apiPathPart

    for {
      resp <- httpClient.callGetString(apiPath, queries = queries)
      body <- resp.body.toFuture("No body data")
      rest <- AttemptRest.toAttempts(body).toFuture()
    } yield rest

  }

  def getAttempt(id:Long):Future[AttemptRest] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}"
    for {
      resp <- httpClient.callGetString(apiPath, Map.empty)
      body <- resp.body.toFuture("No body data")
      rest <- AttemptRest.toAttempt(body).toFuture()
    } yield rest
  }

  def getAttemptRetries(id:Long):Future[List[AttemptRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/retries"
    for {
      resp <- httpClient.callGetString(apiPath, Map.empty)
      body <- resp.body.toFuture("No body data")
      rest <- AttemptRest.toAttempts(body).toFuture()
    } yield rest
  }

  def getTasks(id:Long):Future[List[TaskRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/tasks"
    for {
      resp <- httpClient.callGetString(apiPath, Map.empty)
      body <- resp.body.toFuture("No body data")
      rest <- TaskRest.toTasks(body).toFuture()
    } yield rest
  }

  def startAttempt(workflowId:Long, sessionTime:Instant, retryAttemptName:Option[String] = None,
                   resumeAttemptId:Option[Long] = None, resumeMode:Option[String] = None,
                   paramsJson:Option[String] = None): Future[(AttemptRest,SimpleHttpResponse[String])] = {
    logger.warn("startAttempt called")
    val params = paramsJson.map(Json.toJson(_)).getOrElse(JsObject.empty)
    val areq = AttemptRequestRest(workflowId, sessionTime, retryAttemptName, resumeAttemptId, resumeMode, params)
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}"
    val ret = for {
      resp <- httpClient.callPutString(apiPath, "application/json", Json.toJson(areq).toString())
      body <- resp.body.toFuture("No body data")
      data <- AttemptRest.toAttempt(body).toFuture()
    } yield (data, resp)
    ret
  }
}

