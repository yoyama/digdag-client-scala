package io.github.yoyama.digdag.client

import java.net.URI

import io.github.yoyama.digdag.client.api.{ProjectAPI, SessionAPI, WorkflowAPI}
import io.github.yoyama.digdag.client.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

case class DigdagServerInfo(endPoint:URI, auth:Option[Int] = None, apiWait:FiniteDuration = 30 second) {
  def apiEndPoint(uriPart:String): String = endPoint.toString + uriPart
}

object DigdagServerInfo {
  def local = new DigdagServerInfo(new URI("http://localhost:65432"))
}



class DigdagClient()(implicit val httpClientAkka:HttpClientAkka, val srvInfo:DigdagServerInfo) {
  implicit val projectAPI = new ProjectAPI(httpClientAkka, srvInfo)
  implicit val workflowAPI = new WorkflowAPI(httpClientAkka, srvInfo)
  implicit val sessionAPI = new SessionAPI(httpClientAkka, srvInfo)

  val apiWait = srvInfo.apiWait

  def syncOpt[T](x:Future[Try[T]]): Option[T] = {
    Await.result(x, apiWait) match {
      case Success(value) => Some(value)
      case Failure(exception) => None
    }
  }

  def projects(): Seq[ProjectRest] = syncOpt(projectAPI.getProjects()).getOrElse(Seq.empty)

  def project(name:String): Option[ProjectRest] = syncOpt(projectAPI.getProject(name))

  def project(id:Long): Option[ProjectRest] = syncOpt(projectAPI.getProject(id))

  def workflows(prjId: Long): Option[Seq[WorkflowRest]] = syncOpt(projectAPI.getWorkflows(prjId))

  def workflows(prjName: String): Option[Seq[WorkflowRest]] = {
    for {
      prj <- project(prjName)
      wf  <- workflows(prj.id.toLong)
    } yield wf
  }

  def workflows(prj: ProjectRest): Option[Seq[WorkflowRest]] = workflows(prj.id.toLong)

  def workflow(id:Long): Option[WorkflowRest] = syncOpt(workflowAPI.getWorkflow(id))

  def workflow(prjName:String, name:String): Option[WorkflowRest] = {
    for {
      prj <- project(prjName)
      wf  <- workflow(prj.id, name)
    } yield wf
  }

  def workflow(prjId:Long, name:String): Option[WorkflowRest] = syncOpt(projectAPI.getWorkflow(prjId, name))

  def workflow(prj:ProjectRest, name:String): Option[WorkflowRest] = workflow(prj.id, name)

  def sessions(): Seq[SessionRest] = syncOpt(sessionAPI.getSessions()).getOrElse(Seq.empty)

  def sessions(prjName:String, wfName:String): Seq[SessionRest] = ???

  def session(id:Long): Option[SessionRest] = ???
}

object DigdagClient {
  def apply(srvInfo:DigdagServerInfo = DigdagServerInfo.local): DigdagClient = {
    //ToDo connection check
    new DigdagClient()(new HttpClientAkka, srvInfo)
  }

  def apply(httpClient:HttpClientAkka, srvInfo:DigdagServerInfo): DigdagClient = {
    //ToDo connection check
    new DigdagClient()(httpClient, srvInfo)
  }
}
