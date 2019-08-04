package yoyama.digdag.client

import java.net.URI

import yoyama.digdag.client.http.HttpClientAkka
import yoyama.digdag.client.rest.api.{ProjectAPI, SessionAPI, WorkflowAPI}
import yoyama.digdag.client.rest.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.Await
import scala.concurrent.duration._

case class DigdagServerInfo(endPoint:URI, auth:Option[Int] = None, apiWait:FiniteDuration = 30 second) {
  def apiEndPoint(uriPart:String): String = endPoint.toString + uriPart
}

object DigdagServerInfo {
  def local = new DigdagServerInfo(new URI("http://localhost:65432"))
}



class DigdagClient()(implicit val srvInfo:DigdagServerInfo) {
  implicit val httpClientAkka = new HttpClientAkka
  implicit val projectAPI = new ProjectAPI(httpClientAkka, srvInfo)
  implicit val workflowAPI = new WorkflowAPI(httpClientAkka, srvInfo)
  implicit val sessionAPI = new SessionAPI(httpClientAkka, srvInfo)

  val apiWait = srvInfo.apiWait

  def projects(): Seq[ProjectRest] = {
    Await.result(projectAPI.getProjects(), apiWait).get
  }

  def project(name:String): Option[ProjectRest] = ???

  def project(id:Long): Option[ProjectRest] = ???


  def workflows(prjId: Long): Seq[WorkflowRest] = {
    Await.result(projectAPI.getWorkflows(prjId), apiWait).get
  }

  def workflows(prjName: String): Seq[WorkflowRest] = ???

  def workflows(prj: ProjectRest): Seq[WorkflowRest] = workflows(prj.id.toLong)

  def workflow(id:Long): Option[WorkflowRest] = ???

  def workflow(prjName:String, name:String): Option[WorkflowRest] = ???

  def workflow(prjId:Long, name:String): Option[WorkflowRest] = ???

  def workflow(prj:ProjectRest, name:String): Option[WorkflowRest] = ???

  def sessions(): Seq[SessionRest] = {
    Await.result(sessionAPI.getSessions(), apiWait).get
  }

  def sessions(prjName:String, wfName:String): Seq[SessionRest] = ???

  def session(id:Long): Option[SessionRest] = ???
}

object DigdagClient {
  def apply(srvInfo:DigdagServerInfo = DigdagServerInfo.local): DigdagClient = {
    //ToDo connection check
    new DigdagClient()(srvInfo)
  }
}
