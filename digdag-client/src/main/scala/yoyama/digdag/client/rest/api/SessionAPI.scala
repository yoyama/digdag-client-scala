package yoyama.digdag.client.rest.api

import yoyama.digdag.client.DigdagServerInfo
import yoyama.digdag.client.http.HttpClientAkka
import yoyama.digdag.client.rest.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class SessionAPI(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getSessions():Future[Try[List[SessionRest]]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/sessions"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(SessionRest.toSessions(_))
  }
}

