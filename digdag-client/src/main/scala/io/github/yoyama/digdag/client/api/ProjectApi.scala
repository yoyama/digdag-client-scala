package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo}

import scala.concurrent.Future
import io.github.yoyama.digdag.client.model.{ProjectRest, WorkflowRest}
import io.github.yoyama.digdag.client.commons.Helpers.{HttpClientDigdagHelper}
import io.github.yoyama.digdag.client.http.HttpClientAkkaHttp

class ProjectApi(httpClient: HttpClientAkkaHttp, srvInfo:DigdagServerInfo){

  def getProjects():Future[List[ProjectRest]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    httpClient.callGetToRest(apiPath, Map.empty, ProjectRest.toProjects _)
  }

  def getProjects(name:String): Future[List[ProjectRest]] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    httpClient.callGetToRest(apiPath, Map("name" -> name), ProjectRest.toProjects _)
  }

  def getProject(id:Long) :Future[ProjectRest] = {
    val apiPath = srvInfo.endPoint.toString + s"/api/projects/${id}"
    httpClient.callGetToRest(apiPath, Map.empty, ProjectRest.toProject _)
  }

  def getProject(name:String):Future[ProjectRest] = {
    val apiPath = srvInfo.endPoint.toString + "/api/project"
    httpClient.callGetToRest(apiPath, Map("name" -> name), ProjectRest.toProject _)
  }

  def getWorkflows(prjId:Long): Future[List[WorkflowRest]] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows"
    httpClient.callGetToRest(apiPath, Map.empty, WorkflowRest.toWorkflows _)
  }

  def getWorkflow(prjId:Long, workflowName:String, revision:Option[String] = None): Future[WorkflowRest] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows/${workflowName}"
    val queries = Map[String,Option[String]]("revision" -> revision)
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get))
    httpClient.callGetToRest(apiPath, queries, WorkflowRest.toWorkflow _)
  }
}

