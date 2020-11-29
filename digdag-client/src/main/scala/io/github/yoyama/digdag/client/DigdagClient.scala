package io.github.yoyama.digdag.client

import java.nio.file.Path
import java.time.{Instant, OffsetDateTime}
import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.api.{AttemptApi, ProjectApi, ScheduleApi, SessionApi, VersionApi, WorkflowApi}
import io.github.yoyama.digdag.client.commons.Helpers.{FutureHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig
import io.github.yoyama.digdag.client.http._
import io.github.yoyama.digdag.client.model._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class DigdagClient(val httpClient:SimpleHttpClient, val connInfo:ConnectionConfig) extends ModelUtils {
  val projectApi = new ProjectApi(httpClient, connInfo)
  implicit val workflowApi = new WorkflowApi(httpClient, connInfo)
  implicit val scheduleApi = new ScheduleApi(httpClient, connInfo)
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

  def schedules(lastId:Option[Long] = None): Try[List[ScheduleRest]] = scheduleApi.getSchedules(lastId).syncTry(apiWait)

  def schedule(id:Long): Try[ScheduleRest] = scheduleApi.getSchedule(id).syncTry(apiWait)

  def schEnable(id:Long): Try[ScheduleRest] = scheduleApi.enable(id).syncTry(apiWait)

  def schDisable(id:Long): Try[ScheduleRest] = scheduleApi.disable(id).syncTry(apiWait)

  def schSkip(id:Long, count:Option[Long] = None, fromTime:Option[Instant] = None, nextTime:Option[OffsetDateTime] = None,
              nextRunTime:Option[Instant] = None, dryRun:Boolean = false): Try[ScheduleRest]
          = scheduleApi.skip(id, count, fromTime, nextTime, nextRunTime, dryRun).syncTry(apiWait)

  def schBackfill(id:Long, fromTime:Instant, attemptName:String, dryRun:Boolean = false, count:Option[Long] = None): Try[List[AttemptRest]]
          = scheduleApi.backfill(id, fromTime, attemptName, dryRun, count).syncTry(apiWait)

  def sessions(lastId:Option[Long] = None, pageSize:Option[Long] = None): Try[Seq[SessionRest]] = sessionApi.getSessions(lastId, pageSize).syncTry(apiWait)

  //For convenience and avoid default argument error
  def sessions(prjName:String): Try[Seq[SessionRest]] = sessions(prjName, None, None, None)
  def sessions(prjName:String, wfName:String): Try[Seq[SessionRest]] = sessions(prjName, Option(wfName), None, None)
  def sessions(prjName:String, lastId:Option[Long], pageSize:Option[Long]): Try[Seq[SessionRest]] = sessions(prjName, None, lastId, pageSize)
  def sessions(prjName:String, wfName:String, lastId:Option[Long], pageSize:Option[Long]): Try[Seq[SessionRest]] = sessions(prjName, Option(wfName), None, None)

  def sessions(prjName:String, wfName:Option[String], lastId:Option[Long], pageSize:Option[Long]): Try[Seq[SessionRest]] = {
    val ss = for {
      project <- projectApi.getProject(prjName)
      ss <- projectApi.getSessions(project.id.toLong, wfName, lastId, pageSize)
    } yield ss
    ss.syncTry(apiWait)
  }

  def session(id:Long): Try[SessionRest] = sessionApi.getSession(id).syncTry(apiWait)

  def attempts(sessionId:Long): Try[Seq[AttemptRest]] = sessionApi.getAttempts(sessionId).syncTry(apiWait)

  //For convenience and avoid default argument error
  def attempts(): Try[Seq[AttemptRest]] = attempts(None, None, false, None, None)
  def attempts(lastId:Option[Long], pageSize:Option[Long]): Try[Seq[AttemptRest]] = attempts(None, None, false, lastId, pageSize)
  def attempts(prjName:String): Try[Seq[AttemptRest]] = attempts(Option(prjName), None, false, None, None)
  def attempts(prjName:String, wfName:String): Try[Seq[AttemptRest]] = attempts(Option(prjName), Option(wfName), false, None, None)
  def attempts(prjName:String, wfName:String, includeRetried:Boolean, lastId:Option[Long], pageSize:Option[Long]): Try[Seq[AttemptRest]]
            = attempts(Option(prjName), Option(wfName), includeRetried, lastId, pageSize)

  def attempts(prjName:Option[String], wfName:Option[String], includeRetried:Boolean,
               lastId:Option[Long], pageSize:Option[Long]): Try[Seq[AttemptRest]]
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
