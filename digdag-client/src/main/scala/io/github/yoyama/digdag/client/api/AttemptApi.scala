package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.model.{AttemptRest, SessionRest, TaskRest}
import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

import io.github.yoyama.digdag.client.commons.Helpers.TryHelper

class AttemptApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

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
    for {
      r1 <- httpClient.callGet(apiPath, queries = queries)
      r2 <- r1.asString()
      r3 <- AttemptRest.toAttempts(r2).toFuture
    }  yield r3
  }

  def getAttempt(id:Long):Future[AttemptRest] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- AttemptRest.toAttempt(r2).toFuture
    }  yield r3
  }

  def getAttemptRetries(id:Long):Future[List[AttemptRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/retries"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- AttemptRest.toAttempts(r2).toFuture
    }  yield r3
  }

  def getTasks(id:Long):Future[List[TaskRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/tasks"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- TaskRest.toTasks(r2).toFuture
    }  yield r3
  }

  //def startAttempt


}

