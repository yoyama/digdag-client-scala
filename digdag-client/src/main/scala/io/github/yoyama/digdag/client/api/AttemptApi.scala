package io.github.yoyama.digdag.client.api

import java.time.Instant

import io.github.yoyama.digdag.client.model.{AttemptRest, TaskRest}

import scala.concurrent.{ExecutionContext, Future}
import io.github.yoyama.digdag.client.http.{SimpleHttpClient, SimpleHttpResponse}
import io.github.yoyama.digdag.client.model.request.AttemptRequestRest
import play.api.libs.json.{JsObject, Json}
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, SimpleHttpClientHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig


class AttemptApi(httpClient: SimpleHttpClient, connConfig:ConnectionConfig)(implicit val ec:ExecutionContext)
              extends BasicApi(connConfig) {
  override def apiPathPart = "/api/attempts"


  def getAttempts(prjName: Option[String] = None, wfName: Option[String] = None,
                  includeRetried: Boolean = false, lastId: Option[Long] = None,
                  pageSize: Option[Long] = None): Future[List[AttemptRest]] = {
    def procQueries(): Map[String, String] = {
      val queriesPart: Map[String, String] =
        Seq(("project", prjName), ("workflow", wfName), ("last_id", lastId), ("page_size", pageSize))
          .filter(_._2.isDefined) //remove None
          .map(x => (x._1, x._2.get.toString)) //convert Long to String
          .toMap //Map[String,String]
      queriesPart + (("include_retried", includeRetried.toString))
    }

    def queries: Map[String, String] = procQueries()
    for {
      resp <- httpClient.callGetString(apiPathBase, queries = queries, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- AttemptRest.toAttempts(body).toFuture()
    } yield rest
  }

  def getAttempt(id: Long): Future[AttemptRest] = {
    val apiPath = s"${apiPathBase}/${id}"
    for {
      resp <- httpClient.callGetString(apiPath, queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- AttemptRest.toAttempt(body).toFuture()
    } yield rest
  }

  def getAttemptRetries(id: Long): Future[List[AttemptRest]] = {
    val apiPath = s"${apiPathBase}/${id}/retries"
    for {
      resp <- httpClient.callGetString(apiPath, queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- AttemptRest.toAttempts(body).toFuture()
    } yield rest
  }

  def getTasks(id: Long): Future[List[TaskRest]] = {
    val apiPath = s"${apiPathBase}/${id}/tasks"
    for {
      resp <- httpClient.callGetString(apiPath, queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- TaskRest.toTasks(body).toFuture()
    } yield rest
  }

  def startAttempt(workflowId: Long, sessionTime: Instant, retryAttemptName: Option[String] = None,
                   resumeAttemptId: Option[Long] = None, resumeMode: Option[String] = None,
                   paramsJson: Option[String] = None): Future[(AttemptRest, SimpleHttpResponse[String])] = {
    logger.info("startAttempt called")
    val params = paramsJson.map(Json.toJson(_)).getOrElse(JsObject.empty)
    val areq = AttemptRequestRest(workflowId, sessionTime, retryAttemptName, resumeAttemptId, resumeMode, params)
    val ret = for {
      resp <- httpClient.callPutString(apiPathBase, "application/json", Json.toJson(areq).toString()
                , queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      data <- AttemptRest.toAttempt(body).toFuture()
    } yield (data, resp)
    ret
  }

  def killAttempt(id: Long): Future[Unit] = {
    val apiPath = s"${apiPathBase}/${id}/kill"
    val ret = for {
      resp <- httpClient.callPostString(apiPath, "application/json", null
                , queries = Map.empty, headers = headers())
    } yield resp
    ret.map(_=>())
  }
}

