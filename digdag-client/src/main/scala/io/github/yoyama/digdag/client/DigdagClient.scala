package io.github.yoyama.digdag.client

import java.net.URI
import java.nio.file.Path
import java.time.Instant

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.api.{AttemptApi, ProjectApi, SessionApi, VersionApi, WorkflowApi}
import io.github.yoyama.digdag.client.http._
import io.github.yoyama.digdag.client.model._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

case class ConnectionConfig(name:String, endPoint:URI, auth:Option[Int], apiWait:FiniteDuration) {
  def apiEndPoint(uriPart:String): String = endPoint.toString + uriPart
}

object ConnectionConfig {
  def apply(name:String, uri:String, auth:Option[Int] = None, apiWait:FiniteDuration = 30 second) = new ConnectionConfig(name, new URI(uri), auth, apiWait)

  def local = ConnectionConfig("local", "http://localhost:65432")

  def load(path:Path): Try[ConnectionConfig] = {
    ???
  }

  def loadAll(dir:Path): Map[String, Try[ConnectionConfig]] = {
    ???
  }

  def loadAll(): Map[String, Try[ConnectionConfig]] = {
    ???
  }
}

case class HttpResponseException(val resp:SimpleHttpResponse[String]) extends Throwable

sealed abstract class ResultStatus(val code:Int)
object ResultStatus {
  case object SUCCESS extends ResultStatus(0)
  case object FAIL_DESER extends ResultStatus(1)
  case object FAIL_HTTP extends ResultStatus(2)
  case object FAIL_OTHERS extends ResultStatus(99)
}

// SUCCESS => data, resp
// FAIL_DESER => resp, error
// FAIL_HTTP  => resp, error
// FAIL_OTHERS => error
case class HttpResult[DATA](result:ResultStatus, data:Option[DATA], resp:Option[SimpleHttpResponse[String]],   error:Option[Throwable] = None) extends Throwable

object HttpResult {
  def apply[DATA](resp:Future[(DATA, SimpleHttpResponse[String])], await:FiniteDuration):HttpResult[DATA] = {
    val f:Future[HttpResult[DATA]] = resp
      .map(x => HttpResult(ResultStatus.SUCCESS, Option(x._1), Option(x._2), None))
      .recover {
        case e: HttpResponseException => HttpResult(ResultStatus.FAIL_HTTP, None, Option(e.resp), None)
        case e: Throwable => HttpResult(ResultStatus.FAIL_OTHERS, None, None, Option(e))
      }
    Await.result(f, await)
  }
}

class DigdagClient(val httpClient:SimpleHttpClient)(val connInfo:ConnectionConfig) extends ModelUtils {
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

  def sessions(lastId:Option[Long] = None, pageSize:Option[Long] = None): Option[Seq[SessionRest]] = syncOpt(sessionApi.getSessions(lastId, pageSize))

  def sessions(prjName:String, wfName:String): Option[Seq[SessionRest]] = ??? //syncOpt(sessionApi.getSessions())

  def session(id:Long): Option[SessionRest] = syncOpt(sessionApi.getSession(id))

  def attempts(sessionId:Long): Option[Seq[AttemptRest]] = syncOpt(sessionApi.getAttempts(sessionId))

  def attempts(prjName:String, wfName:String, includeRetried:Boolean = false,
               lastId:Option[Long] = None, pageSize:Option[Long] = None  ): Option[Seq[AttemptRest]]
                            = syncOpt(attemptApi.getAttempts(prjName, wfName))

  def attempt(id:Long): Option[AttemptRest] = syncOpt(attemptApi.getAttempt(id))

  def retries(attemptId:Long): Option[List[AttemptRest]] = syncOpt(attemptApi.getAttemptRetries(attemptId))

  def tasks(attemptId:Long): Option[List[TaskRest]] = syncOpt(attemptApi.getTasks(attemptId))

  def doKill(attemptId:Long) = ???

  //def doPush

  def doStart(prjName:String, wfName:String, session:Option[String] = None): HttpResult[AttemptRest] = {
    def getSession = session.map(toInstant(_)).getOrElse(Instant.now())
    val ret = for {
      prj <- projectApi.getProject(prjName)
      wf <- projectApi.getWorkflow(Integer.parseInt(prj.id), wfName)
      apiResult <- attemptApi.startAttempt(Integer.parseInt(wf.id), getSession)
    } yield apiResult
    HttpResult(ret, connInfo.apiWait)
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

  def version():Try[String] = syncTry(versionApi.getVersion()).map(_.version)

}

object DigdagClient {
  def apply(connInfo:ConnectionConfig = ConnectionConfig.local): DigdagClient = {
    new DigdagClient(new SimpleHttpClientScalaJ)( connInfo)
  }

  def apply(httpClient: SimpleHttpClient, srvInfo:ConnectionConfig): DigdagClient = {
    new DigdagClient(httpClient)(srvInfo)
  }
}
