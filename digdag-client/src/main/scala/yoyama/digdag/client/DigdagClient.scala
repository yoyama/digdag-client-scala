package yoyama.digdag.client

import java.net.URI

import yoyama.digdag.client.http.HttpClientAkka
import yoyama.digdag.client.rest.api.ProjectAPI
import yoyama.digdag.client.rest.model.{ProjectRest, WorkflowRest}

import scala.concurrent.Await
import scala.concurrent.duration._

case class DigdagServerInfo(endPoint:URI, auth:Option[Int] = None)

object DigdagServerInfo {
  def local = new DigdagServerInfo(new URI("http://localhost:65432"))
}



class DigdagClient()(implicit val srvInfo:DigdagServerInfo) {
  implicit val httpClientAkka = new HttpClientAkka
  implicit val projectAPI = new ProjectAPI

  def getProjects():Seq[ProjectRest] = {
    Await.result(projectAPI.getProjects(), 30 second).get
  }

  def getWorkflows(prjId:Long):Seq[WorkflowRest] = {
    Await.result(projectAPI.getWorkflows(prjId), 30 second).get
  }
}

object DigdagClient {
  def apply(srvInfo:DigdagServerInfo = DigdagServerInfo.local): DigdagClient = {
    //ToDo connection check
    new DigdagClient()(srvInfo)
  }
}
