package io.github.yoyama.digdag.client

import java.net.URI

import io.github.yoyama.digdag.client.api.{ProjectApi, SessionApi, WorkflowApi}
import io.github.yoyama.digdag.client.model.{ProjectRest, SessionRest, WorkflowRest}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps

case class DigdagServerInfo(endPoint:URI, auth:Option[Int], apiWait:FiniteDuration) {
  def apiEndPoint(uriPart:String): String = endPoint.toString + uriPart
}

object DigdagServerInfo {
  def apply(uri:String, auth:Option[Int] = None, apiWait:FiniteDuration = 30 second) = new DigdagServerInfo(new URI(uri), auth, apiWait)

  def local = DigdagServerInfo("http://localhost:65432")
}



class DigdagClient()(implicit val httpClientAkka:HttpClientAkka, val srvInfo:DigdagServerInfo) {
  implicit val projectApi = new ProjectApi(httpClientAkka, srvInfo)
  implicit val workflowApi = new WorkflowApi(httpClientAkka, srvInfo)
  implicit val sessionApi = new SessionApi(httpClientAkka, srvInfo)

  val apiWait = srvInfo.apiWait

  def syncOpt[T](x:Future[Try[T]]): Option[T] = {
    Await.result(x, apiWait) match {
      case Success(value) => Some(value)
      case Failure(exception) => None
    }
  }

  def projects(): Seq[ProjectRest] = syncOpt(projectApi.getProjects()).getOrElse(Seq.empty)

  def project(name:String): Option[ProjectRest] = syncOpt(projectApi.getProject(name))

  def project(id:Long): Option[ProjectRest] = syncOpt(projectApi.getProject(id))

  def workflows(prjId: Long): Option[Seq[WorkflowRest]] = syncOpt(projectApi.getWorkflows(prjId))

  def workflows(prjName: String): Option[Seq[WorkflowRest]] = {
    for {
      prj <- project(prjName)
      wf  <- workflows(prj.id.toLong)
    } yield wf
  }

  def workflows(prj: ProjectRest): Option[Seq[WorkflowRest]] = workflows(prj.id.toLong)

  def workflow(id:Long): Option[WorkflowRest] = syncOpt(workflowApi.getWorkflow(id))

  def workflow(prjName:String, name:String): Option[WorkflowRest] = {
    for {
      prj <- project(prjName)
      wf  <- workflow(prj.id, name)
    } yield wf
  }

  def workflow(prjId:Long, name:String): Option[WorkflowRest] = syncOpt(projectApi.getWorkflow(prjId, name))

  def workflow(prj:ProjectRest, name:String): Option[WorkflowRest] = workflow(prj.id, name)

  def sessions(): Option[Seq[SessionRest]] = syncOpt(sessionApi.getSessions())

  def sessions(prjName:String, wfName:String): Option[Seq[SessionRest]] = syncOpt(sessionApi.getSessions())

  def session(id:Long): Option[SessionRest] = syncOpt(sessionApi.getSession(id))
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
