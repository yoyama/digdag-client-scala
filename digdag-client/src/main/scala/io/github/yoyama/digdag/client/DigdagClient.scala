package io.github.yoyama.digdag.client

import java.net.URI
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.api.{AttemptApi, ProjectApi, SessionApi, WorkflowApi}
import io.github.yoyama.digdag.client.http.{HttpClientAkkaHttp, HttpResponseException, ResultStatus}
import io.github.yoyama.digdag.client.model._
import wvlet.airframe.http.HttpResponse

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.model.{ErrorInfo, Multipart, RequestEntity, StatusCode, ContentType => AkkaContentType, ContentTypes => AkkaContentTypes, HttpEntity => AkkaHttpEntity, HttpMessage => AkkaHttpMessage, HttpMethods => AkkaHttpMethods, HttpRequest => AkkaHttpRequest, HttpResponse => AkkaHttpResponse, Uri => AkkaUri}

case class DigdagServerInfo(endPoint:URI, auth:Option[Int], apiWait:FiniteDuration) {
  def apiEndPoint(uriPart:String): String = endPoint.toString + uriPart
}

object DigdagServerInfo {
  def apply(uri:String, auth:Option[Int] = None, apiWait:FiniteDuration = 30 second) = new DigdagServerInfo(new URI(uri), auth, apiWait)

  def local = DigdagServerInfo("http://localhost:65432")
}

// SUCCESS => data, resp
// FAIL_DESER => resp, error
// FAIL_HTTP  => resp, error
// FAIL_OTHERS => error
case class HttpResult[DATA,RESP <: HttpResponse[_]](result:ResultStatus, data:Option[DATA], resp:Option[RESP],   error:Option[Throwable] = None) extends Throwable

object HttpResult {
  def apply[DATA, RESP <:HttpResponse[_]](resp:Future[(DATA,RESP)], await:FiniteDuration)
                                           (implicit tag:ClassTag[RESP]):HttpResult[DATA,RESP] = {
    val f:Future[HttpResult[DATA,RESP]] = resp
      .map(x => HttpResult(ResultStatus.SUCCESS, Option(x._1), Option(x._2), None))
      .recover {
        case e: HttpResponseException[RESP] => HttpResult(ResultStatus.FAIL_HTTP, None, Option(e.resp), None)
        case e: Throwable => HttpResult(ResultStatus.FAIL_OTHERS, None, None, Option(e))
      }
    Await.result(f, await)
  }
}

class DigdagClient()(implicit val httpClient:HttpClientAkkaHttp, val srvInfo:DigdagServerInfo) extends ModelUtils {
  implicit val projectApi = new ProjectApi(httpClient, srvInfo)
  implicit val workflowApi = new WorkflowApi(httpClient, srvInfo)
  implicit val sessionApi = new SessionApi(httpClient, srvInfo)
  implicit val attemptApi = new AttemptApi(httpClient, srvInfo)

  val apiWait = srvInfo.apiWait

  def syncOpt[T](x:Future[T]): Option[T] = {
    val f = x.map(Option(_)).recover {
      case _ => None
    }
    Await.result(f, srvInfo.apiWait)
  }

  def syncTry[T](x:Future[T]): Try[T] = {
    val f = x.map(Success(_)).recover {
      case e:Throwable => Failure(e)
      case x => Failure(new Throwable(x))
    }
    Await.result(f, srvInfo.apiWait)
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

  def doStart(prjName:String, wfName:String, session:Option[String] = None): HttpResult[AttemptRest, HttpResponse[_]] = {
    def getSession = session.map(toInstant(_)).getOrElse(Instant.now())
    val ret: Future[(AttemptRest,HttpResponse[_])] = for {
      prj <- projectApi.getProject(prjName)
      wf <- projectApi.getWorkflow(Integer.parseInt(prj.id), wfName)
      apiResult <- attemptApi.startAttempt(Integer.parseInt(wf.id), getSession)
    } yield apiResult
    HttpResult(ret, srvInfo.apiWait)
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

}

object DigdagClient {
  def apply(srvInfo:DigdagServerInfo = DigdagServerInfo.local): DigdagClient = {
    implicit val timeout = 60 seconds
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    new DigdagClient()(new HttpClientAkkaHttp(), srvInfo)
  }

  def apply(httpClient:HttpClientAkkaHttp, srvInfo:DigdagServerInfo): DigdagClient = {
    implicit val timeout = srvInfo.apiWait
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    new DigdagClient()(httpClient, srvInfo)
  }
}
