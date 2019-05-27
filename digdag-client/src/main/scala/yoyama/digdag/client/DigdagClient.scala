package yoyama.digdag.client

import java.net.URI

import yoyama.digdag.client.http.HttpClientAkka
import yoyama.digdag.client.rest.api.{ProjectAPI, SessionAPI}
import yoyama.digdag.client.rest.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.Await
import scala.concurrent.duration._

case class DigdagServerInfo(endPoint:URI, auth:Option[Int] = None)

object DigdagServerInfo {
  def local = new DigdagServerInfo(new URI("http://localhost:65432"))
}



class DigdagClient(apiWait:FiniteDuration = 30 second)(implicit val srvInfo:DigdagServerInfo) {
  implicit val httpClientAkka = new HttpClientAkka
  implicit val projectAPI = new ProjectAPI(httpClientAkka, srvInfo)
  implicit val sessionAPI = new SessionAPI(httpClientAkka, srvInfo)

  def projects(): Seq[ProjectRest] = {
    Await.result(projectAPI.getProjects(), apiWait).get
  }

  def workflows(prjId: Long): Seq[WorkflowRest] = {
    Await.result(projectAPI.getWorkflows(prjId), apiWait).get
  }

  def workflows(prj: ProjectRest): Seq[WorkflowRest] = workflows(prj.id.toLong)

  def sessions(): Seq[SessionRest] = {
    Await.result(sessionAPI.getSessions(), apiWait).get
  }
}

object DigdagClient {
  def apply(srvInfo:DigdagServerInfo = DigdagServerInfo.local): DigdagClient = {
    //ToDo connection check
    new DigdagClient()(srvInfo)
  }
}
