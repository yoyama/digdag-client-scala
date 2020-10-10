package io.github.yoyama.digdag.client

import java.nio.file.Path
import java.time.Instant
import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.api.{AttemptApi, ProjectApi, SessionApi, VersionApi, WorkflowApi}
import io.github.yoyama.digdag.client.commons.Helpers.TryHelper
import io.github.yoyama.digdag.client.http._
import io.github.yoyama.digdag.client.model._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class DigdagClient(val httpClient:SimpleHttpClient, val connInfo:ConnectionConfig) extends ModelUtils {
  implicit val projectApi = new ProjectApi(httpClient, connInfo)
  implicit val workflowApi = new WorkflowApi(httpClient, connInfo)
  implicit val sessionApi = new SessionApi(httpClient, connInfo)
  implicit val attemptApi = new AttemptApi(httpClient, connInfo)
  implicit val versionApi = new VersionApi(httpClient, connInfo)

  val apiWait = connInfo.apiWait

  def syncOpt[T](x:Future[T]): Option[T] = {
    val f = x.map(Option(_)).recover {
      case _ => None
    }
    Await.result(f, connInfo.apiWait)
  }

  def syncTry[T](x:Future[T]): Try[T] = {
    val f = x.map(Success(_)).recover {
      case e:Throwable => Failure(e)
      case x => Failure(new Throwable(x))
    }
    Await.result(f, connInfo.apiWait)
  }

  def projects(): Try[Seq[ProjectRest]] = syncTry(projectApi.getProjects())

  def project(name:String): Try[ProjectRest] = syncTry(projectApi.getProject(name))

  def project(id:Long): Try[ProjectRest] = syncTry(projectApi.getProject(id))

  def workflows(prjId: Long): Try[Seq[WorkflowRest]] = syncTry(projectApi.getWorkflows(prjId))

  def workflows(prjName: String): Try[Seq[WorkflowRest]] = {
    for {
      prj <- project(prjName)
      wf  <- workflows(prj.id.toLong)
    } yield wf
  }

  def workflows(prj: ProjectRest): Try[Seq[WorkflowRest]] = workflows(prj.id.toLong)

  def workflow(id:Long): Try[WorkflowRest] = syncTry(workflowApi.getWorkflow(id))

  def workflow(prjName:String, name:String): Try[WorkflowRest] = {
    for {
      prj <- project(prjName)
      wf  <- workflow(prj.id, name)
    } yield wf
  }

  def workflow(prjId:Long, name:String): Try[WorkflowRest] = syncTry(projectApi.getWorkflow(prjId, name))

  def workflow(prj:ProjectRest, name:String): Try[WorkflowRest] = workflow(prj.id, name)

  def sessions(lastId:Option[Long] = None, pageSize:Option[Long] = None): Try[Seq[SessionRest]] = syncTry(sessionApi.getSessions(lastId, pageSize))

  def sessions(prjName:String, wfName:String): Try[Seq[SessionRest]] = ??? //syncOpt(sessionApi.getSessions())

  def session(id:Long): Try[SessionRest] = syncTry(sessionApi.getSession(id))

  def attempts(sessionId:Long): Try[Seq[AttemptRest]] = syncTry(sessionApi.getAttempts(sessionId))

  def attempts(prjName:Option[String] = None, wfName:Option[String] = None, includeRetried:Boolean = false,
               lastId:Option[Long] = None, pageSize:Option[Long] = None  ): Try[Seq[AttemptRest]]
                            = syncTry(attemptApi.getAttempts(prjName, wfName, includeRetried, lastId, pageSize))

  def attempt(id:Long): Try[AttemptRest] = syncTry(attemptApi.getAttempt(id))

  def retries(attemptId:Long): Try[List[AttemptRest]] = syncTry(attemptApi.getAttemptRetries(attemptId))

  def tasks(attemptId:Long): Try[List[TaskRest]] = syncTry(attemptApi.getTasks(attemptId))

  def doPush(prjName:String, prjDir:Path, revision:Option[String] = None): Try[ProjectRest] = {
    syncTry(projectApi.pushProjectDir(prjName, revision.getOrElse(UUID.randomUUID.toString), prjDir))
  }

  def doStart(prjName:String, wfName:String, session:Option[String] = None): Try[AttemptRest] = {
    def getSession: Try[Instant] = session match {
      case None => Success(Instant.now())
      case Some(s) => toInstant(s)
    }
    val ret = for {
      ss <- getSession.toFuture()
      prj <- projectApi.getProject(prjName)
      wf <- projectApi.getWorkflow(Integer.parseInt(prj.id), wfName)
      apiResult <- attemptApi.startAttempt(Integer.parseInt(wf.id), ss)
    } yield apiResult._1
    syncTry(ret)
  }

  def doKill(attemptId:Long): Try[AttemptRest] = syncTry{
    for {
      unit <- attemptApi.killAttempt(attemptId)
      attempt <- attemptApi.getAttempt(attemptId)
    } yield attempt
  }

  //def schedules

  //def schedule

  //def doScheduleUpdate
  //def doScheduleBackfill
  //def doScheduleSkip
  //def doScheduleEnable
  //def doScheduleDisable

  //def do(Add|Del)Secrets

  //def logFiles(projId)
  //def logDownload(projId)

  def version:Try[String] = syncTry(versionApi.getVersion()).map(_.version)

}

object DigdagClient {
  def apply(connInfo:ConnectionConfig = ConnectionConfig.local): DigdagClient = {
    new DigdagClient(new SimpleHttpClientScalaJ, connInfo)
  }

  def apply(httpClient: SimpleHttpClient, connInfo:ConnectionConfig): DigdagClient = {
    new DigdagClient(httpClient, connInfo)
  }
}
