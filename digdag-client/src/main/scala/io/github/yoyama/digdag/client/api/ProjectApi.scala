package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import io.github.yoyama.digdag.client.model.{ProjectRest, WorkflowRest}
import io.github.yoyama.digdag.client.commons.Helpers.TryHelper

class ProjectApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getProjects():Future[List[ProjectRest]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- ProjectRest.toProjects(r2).toFuture
    }  yield r3
  }

  def getProjects(name:String): Future[List[ProjectRest]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    for {
      r1 <- httpClient.callGet(apiPath, Map("name" -> name))
      r2 <- r1.asString()
      r3 <- ProjectRest.toProjects(r2).toFuture
    }  yield r3
  }

  def getProject(id:Long) :Future[ProjectRest] = {
    val apiPath = srvInfo.endPoint.toString + s"/api/projects/${id}"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- ProjectRest.toProject(r2).toFuture
    }  yield r3
  }

  def getProject(name:String):Future[ProjectRest] = {
    val apiPath = srvInfo.endPoint.toString + "/api/project"
    val queries = Map("name" -> name)
    for {
      r1 <- httpClient.callGet(apiPath, queries)
      r2 <- r1.asString()
      r3 <- ProjectRest.toProject(r2).toFuture
    }  yield r3
  }

  def getWorkflows(prjId:Long): Future[List[WorkflowRest]] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows"
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- WorkflowRest.toWorkflows(r2).toFuture
    }  yield r3
  }

  def getWorkflow(prjId:Long, workflowName:String, revision:Option[String] = None): Future[WorkflowRest] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows/${workflowName}"
    val queries = Map[String,Option[String]]("revision" -> revision)
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get))
    for {
      r1 <- httpClient.callGet(apiPath, queries)
      r2 <- r1.asString()
      r3 <- WorkflowRest.toWorkflow(r2).toFuture
    }  yield r3
  }
}

