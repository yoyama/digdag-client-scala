package yoyama.digdag.client.rest.api

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import yoyama.digdag.client.http.HttpClientAkka
import yoyama.digdag.client.rest.model.{ProjectRest, WorkflowRest}
import yoyama.digdag.client.DigdagServerInfo

class ProjectAPI(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

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

  def getWorkflows(prjId:Long): Future[Try[List[WorkflowRest]]] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/workflows"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(WorkflowRest.toWorkflows(_))
  }

  def getWorkflow(name:String)(): Future[Try[WorkflowRest]] = ???

}

