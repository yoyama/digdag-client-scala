package io.github.yoyama.digdag.client

import java.nio.file.Path
import java.time.Instant
import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.api.{AttemptApi, ProjectApi, SessionApi, VersionApi, WorkflowApi}
import io.github.yoyama.digdag.client.commons.Helpers.{FutureHelper,TryHelper}
import io.github.yoyama.digdag.client.http._
import io.github.yoyama.digdag.client.model._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class DigdagClient(val httpClient:SimpleHttpClient, val connInfo:ConnectionConfig) extends ModelUtils {
  val projectApi = new ProjectApi(httpClient, connInfo)
  implicit val workflowApi = new WorkflowApi(httpClient, connInfo)
  implicit val sessionApi = new SessionApi(httpClient, connInfo)
  implicit val attemptApi = new AttemptApi(httpClient, connInfo)
  implicit val versionApi = new VersionApi(httpClient, connInfo)

  val apiWait = connInfo.apiWait

  def projects(): Try[Seq[ProjectRest]] = projectApi.getProjects().syncTry(apiWait)

  def project(name:String): Try[ProjectRest] = projectApi.getProject(name).syncTry(apiWait)

  def project(id:Long): Try[ProjectRest] = projectApi.getProject(id).syncTry(apiWait)

  def workflows(prjId: Long): Try[Seq[WorkflowRest]] = projectApi.getWorkflows(prjId).syncTry(apiWait)

  def workflows(prjName: String): Try[Seq[WorkflowRest]] = {
    for {
      prj <- project(prjName)
      wf  <- workflows(prj.id.toLong)
    } yield wf
  }

  def workflows(prj: ProjectRest): Try[Seq[WorkflowRest]] = workflows(prj.id.toLong)

  def workflow(id:Long): Try[WorkflowRest] = workflowApi.getWorkflow(id).syncTry(apiWait)

  def workflow(prjName:String, name:String): Try[WorkflowRest] = {
    for {
      prj <- project(prjName)
      wf  <- workflow(prj.id, name)
    } yield wf
  }

  def workflow(prjId:Long, name:String): Try[WorkflowRest] = projectApi.getWorkflow(prjId, name).syncTry(apiWait)

  def workflow(prj:ProjectRest, name:String): Try[WorkflowRest] = workflow(prj.id, name)

  def sessions(lastId:Option[Long] = None, pageSize:Option[Long] = None): Try[Seq[SessionRest]] = sessionApi.getSessions(lastId, pageSize).syncTry(apiWait)

  def sessions(prjName:String, wfName:String): Try[Seq[SessionRest]] = ??? //syncOpt(sessionApi.getSessions())

  def session(id:Long): Try[SessionRest] = sessionApi.getSession(id).syncTry(apiWait)

  def attempts(sessionId:Long): Try[Seq[AttemptRest]] = sessionApi.getAttempts(sessionId).syncTry(apiWait)

  def attempts(prjName:Option[String] = None, wfName:Option[String] = None, includeRetried:Boolean = false,
               lastId:Option[Long] = None, pageSize:Option[Long] = None  ): Try[Seq[AttemptRest]]
        = attemptApi.getAttempts(prjName, wfName, includeRetried, lastId, pageSize).syncTry(apiWait)

  def attempt(id:Long): Try[AttemptRest] = attemptApi.getAttempt(id).syncTry(apiWait)

  def retries(attemptId:Long): Try[List[AttemptRest]] = attemptApi.getAttemptRetries(attemptId).syncTry(apiWait)

  def tasks(attemptId:Long): Try[List[TaskRest]] = attemptApi.getTasks(attemptId).syncTry(apiWait)

  def pushProject(prjName:String, prjDir:Path, revision:Option[String] = None): Try[ProjectRest]
        = projectApi.pushProjectDir(prjName, revision.getOrElse(UUID.randomUUID.toString), prjDir).syncTry(apiWait)

  def startAttempt(prjName:String, wfName:String, session:Option[String] = None): Try[AttemptRest] = {
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
    ret.syncTry(apiWait)
  }

  def killAttempt(attemptId:Long): Try[AttemptRest] = {
    val f = for {
      unit <- attemptApi.killAttempt(attemptId)
      attempt <- attemptApi.getAttempt(attemptId)
    } yield attempt
    f.syncTry(apiWait)
  }

  def secrets(prjId:Long):Try[Seq[String]] = {
    projectApi.getSecretKeys(prjId).map(_.keys).syncTry(apiWait)
  }

  def addSecret(prjId:Long, key:String, value:String):Try[Unit] = {
    projectApi.putSecret(prjId, key, value).syncTry(apiWait)
  }

  def deleteSecret(prjId:Long, key:String):Try[Unit] = {
    projectApi.deleteSecret(prjId, key).syncTry(apiWait)
  }

  //def schedules

  //def schedule

  //def doScheduleUpdate
  //def doScheduleBackfill
  //def doScheduleSkip
  //def doScheduleEnable
  //def doScheduleDisable

  //def logFiles(projId)
  //def logDownload(projId)

  def version:Try[String] = versionApi.getVersion().map(_.version).syncTry(apiWait)

}

object DigdagClient {
  def apply(connInfo:ConnectionConfig = ConnectionConfig.local): DigdagClient = {
    new DigdagClient(new SimpleHttpClientScalaJ, connInfo)
  }

  def apply(httpClient: SimpleHttpClient, connInfo:ConnectionConfig): DigdagClient = {
    new DigdagClient(httpClient, connInfo)
  }
}
