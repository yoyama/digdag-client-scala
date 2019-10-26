package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}
import io.github.yoyama.digdag.client.model.{AttemptRest, ProjectRest, SessionRest, WorkflowRest}
import io.github.yoyama.digdag.client.commons.Helpers.TryHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class SessionApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getSessions(lastId:Option[Long] = None, pageSize:Option[Long] = None):Future[List[SessionRest]] = {
    val queries =
      Seq(("last_id", lastId), ("page_size", pageSize))
        .filter(_._2.isDefined)            //remove None
        .map(x => (x._1, x._2.toString))   //convert Long to String
        .toMap                             //Map[String,String]
    val apiPath = srvInfo.endPoint.toString + "/api/sessions"
    for {
      r1 <- httpClient.callGet(apiPath, queries)
      r2 <- r1.asString()
      r3 <- SessionRest.toSessions(r2).toFuture
    } yield r3
  }

  def getSession(id:Long):Future[SessionRest] = {
    val apiPath = srvInfo.endPoint.toString + s"/api/sessions/${id}"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- SessionRest.toSession(r2).toFuture
    } yield r3
  }

  def getAttempts(id:Long):Future[List[AttemptRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"/api/sessions/${id}/attempts"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- AttemptRest.toAttempts(r2).toFuture
    } yield r3
  }
}

