package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}
import io.github.yoyama.digdag.client.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class SessionApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getSessions(lastId:Option[Long] = None, pageSize:Option[Long] = None):Future[Try[List[SessionRest]]] = {
    val queries =
      Seq(("last_id", lastId), ("page_size", pageSize))
        .filter(_._2.isDefined)            //remove None
        .map(x => (x._1, x._2.toString))   //convert Long to String
        .toMap                             //Map[String,String]
    val apiPath = srvInfo.endPoint.toString + "/api/sessions"
    val responseF = httpClient.callGet(apiPath, queries = queries)
    responseF.flatMap(_.asString()).map(SessionRest.toSessions(_))
  }

  def getSession(id:Long):Future[Try[SessionRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"/api/sessions/${id}"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(SessionRest.toSession(_))
  }
}

