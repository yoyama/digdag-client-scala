package io.github.yoyama.digdag.client.api

import java.nio.file.{Files, Path}
import java.time.LocalDateTime

import io.github.yoyama.digdag.client.ConnectionConfig
import io.github.yoyama.digdag.client.commons.ArchiveUtils

import scala.concurrent.{ExecutionContext, Future}
import io.github.yoyama.digdag.client.model.{ProjectRest, SecretKeysRest, WorkflowRest}
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, SimpleHttpClientHelper, TryHelper}
import io.github.yoyama.digdag.client.http.SimpleHttpClient

import scala.util.Random
import scala.util.control.Exception.catching

class ProjectApi(httpClient: SimpleHttpClient, srvInfo:ConnectionConfig)(implicit val ec:ExecutionContext) {
  val archiveUtils = new ArchiveUtils {}

  def pushProjectDir(name:String, revision:String, dir:Path, tempDir:Path = archiveUtils.sysTempDir): Future[ProjectRest] = {
    for {
      archivePath <- catching(classOf[Throwable])
        .withTry(tempDir.resolve(s"digdag_prject_${System.currentTimeMillis}_${Random.nextLong()}.tar.gz"))
        .toFuture()
      archiveFile <- Future {
        val f = archivePath.toFile
        f.deleteOnExit()
        f
      }
      tar <- archiveUtils.archiveProject(archiveFile.toPath, dir)
      rest <- pushProject(name, revision, tar.toPath)
    } yield rest
  }

  def pushProject(name:String, revision:String, archive:Path): Future[ProjectRest] = {
    val apiPath = srvInfo.endPoint.toString + "/api/projects"
    val queries = Map("project" -> name, "revision" -> revision)
    for {
      resp <- httpClient.callPutUpload(apiPath, "application/gzip", archive, queries)
      body <- resp.body.toFuture("No body data")
      rest <- ProjectRest.toProject(body).toFuture()
    } yield rest
  }

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

  def getSecretKeys(prjId:Long): Future[SecretKeysRest] = {
    val apiPath = srvInfo.endPoint.toASCIIString + s"/api/projects/${prjId}/secrets"
    httpClient.callGetToRest(apiPath, Map.empty, SecretKeysRest.toSecretKeysRest _)
  }
}

