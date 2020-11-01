package io.github.yoyama.digdag.client.api

import java.nio.file.{Files, Path}

import io.github.yoyama.digdag.client.commons.ArchiveUtils

import scala.concurrent.{ExecutionContext, Future}
import io.github.yoyama.digdag.client.model.{ProjectRest, SecretKeysRest, WorkflowRest}
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, SimpleHttpClientHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig
import io.github.yoyama.digdag.client.http.SimpleHttpClient

import scala.util.Random
import scala.util.control.Exception.catching

class ProjectApi(httpClient: SimpleHttpClient, connConfig:ConnectionConfig)(implicit val ec:ExecutionContext)
                          extends BasicApi(connConfig) {
  override def apiPathPart = "/api/projects"

  val archiveUtils = new ArchiveUtils {}

  def pushProjectDir(name: String, revision: String, dir: Path, tempDir: Path = archiveUtils.sysTempDir): Future[ProjectRest] = {
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

  def pushProject(name: String, revision: String, archive: Path): Future[ProjectRest] = {
    val apiPath = connConfig.endPoint.toString + "/api/projects"
    val queries = Map("project" -> name, "revision" -> revision)
    for {
      resp <- httpClient.callPutUpload(apiPath, "application/gzip", archive, queries = queries, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- ProjectRest.toProject(body).toFuture()
    } yield rest
  }

  def getProjects(): Future[List[ProjectRest]] = {
    httpClient.callGetToRest(apiPathBase, ProjectRest.toProjects _, headers = headers())
  }

  def getProjects(name: String): Future[List[ProjectRest]] = {
    httpClient.callGetToRest(apiPathBase, ProjectRest.toProjects _, Map("name" -> name), headers = headers())
  }

  def getProject(id: Long): Future[ProjectRest] = {
    val apiPath = s"${apiPathBase}/${id}"
    httpClient.callGetToRest(apiPath,ProjectRest.toProject _, headers = headers())
  }

  def getProject(name: String): Future[ProjectRest] = {
    httpClient.callGetToRest(apiPathBase, ProjectRest.toProject _, Map("name" -> name), headers = headers())
  }

  def getWorkflows(prjId: Long): Future[List[WorkflowRest]] = {
    val apiPath = s"${apiPathBase}/${prjId}/workflows"
    httpClient.callGetToRest(apiPath, WorkflowRest.toWorkflows _, headers = headers())
  }

  def getWorkflow(prjId: Long, workflowName: String, revision: Option[String] = None): Future[WorkflowRest] = {
    val apiPath = s"${apiPathBase}/${prjId}/workflows/${workflowName}"
    val queries = Map[String, Option[String]]("revision" -> revision)
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get))
    httpClient.callGetToRest(apiPath, WorkflowRest.toWorkflow _, queries = queries, headers = headers())
  }

  def getSecretKeys(prjId: Long): Future[SecretKeysRest] = {
    val apiPath = s"${apiPathBase}/${prjId}/secrets"
    httpClient.callGetToRest(apiPath, SecretKeysRest.toSecretKeysRest _, headers = headers())
  }

  def putSecret(prjId: Long, keyName: String, keyValue: String): Future[Unit] = {
    val apiPath = s"${apiPathBase}/${prjId}/secrets/${keyName}"
    httpClient.callPutString(apiPath, "application/json", s"""{"value" : "${keyValue}"}"""
                  , headers = headers()).map(_ => ())
  }

  def deleteSecret(prjId: Long, keyName: String): Future[Unit] = {
    import io.github.yoyama.digdag.client.http.SimpleHttpClient.unitConverter
    val apiPath = s"${apiPathBase}/${prjId}/secrets/${keyName}"
    httpClient.callDelete(apiPath, headers = headers())(unitConverter).map(_ => ())
  }
}

