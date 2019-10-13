package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import io.github.yoyama.digdag.client.model.{ProjectRest, WorkflowRest}

class ProjectApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getProjects():Future[Try[List[ProjectRest]]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(ProjectRest.toProjects(_))
  }

  def getProjects(name:String): Future[Try[List[ProjectRest]]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    val responseF = httpClient.callGet(apiPath, Map("name" -> name))
    responseF.flatMap(_.asString()).map(ProjectRest.toProjects(_))
  }

  def getProject(id:Long) :Future[Try[ProjectRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"/api/projects/${id}"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(ProjectRest.toProject(_))
  }

  def getProject(name:String):Future[Try[ProjectRest]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/project"
    val queries = Map("name" -> name)
    val responseF = httpClient.callGet(apiPath, queries)
    responseF.flatMap(_.asString()).map(ProjectRest.toProject(_))
  }

  def getWorkflows(prjId:Long): Future[Try[List[WorkflowRest]]] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(WorkflowRest.toWorkflows(_))
  }

  def getWorkflow(prjId:Long, workflowName:String, revision:Option[String] = None): Future[Try[WorkflowRest]] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows/${workflowName}"
    val queries = Map[String,Option[String]]("revision" -> revision)
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get))
    val responseF = httpClient.callGet(apiPath, queries)
    responseF.flatMap(_.asString()).map(WorkflowRest.toWorkflow(_))
  }
}

