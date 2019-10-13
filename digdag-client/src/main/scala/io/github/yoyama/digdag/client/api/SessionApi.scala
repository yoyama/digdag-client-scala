package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}
import io.github.yoyama.digdag.client.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class SessionApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getSessions():Future[Try[List[SessionRest]]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/sessions"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(SessionRest.toSessions(_))
  }
}

