package io.github.yoyama.digdag.client.api

import java.io.{ByteArrayInputStream, File, IOException}
import java.nio.file.{Files, Path, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import com.twitter.finagle.http.{Response, Status}
import io.github.yoyama.digdag.client.http.SimpleHttpClientScalaJ
import wvlet.airframe.Design
import wvlet.airframe.http.{HttpMessage, HttpRequest, Router}
import java.util.zip.GZIPInputStream

import io.github.yoyama.digdag.client.commons.IOUtils
import io.github.yoyama.digdag.client.config.ConnectionConfig
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success}
import scala.util.control.Exception._

private[api] trait ApiDigdagServerMockFixture extends IOUtils {

  import wvlet.airframe.http.finagle._
  import wvlet.airframe.http.{Endpoint, HttpMethod}

  def serverPort = 55432
  val endPointURL = s"http://localhost:${serverPort}/api"
  val connInfo = ConnectionConfig("test", s"http://localhost:${serverPort}")
  val httpClient = new SimpleHttpClientScalaJ()

  val projectApi = new ProjectApi(httpClient, connInfo)
  val workflowApi = new WorkflowApi(httpClient, connInfo)
  val sessionApi = new SessionApi(httpClient, connInfo)
  val attemptApi = new AttemptApi(httpClient, connInfo)

  val router = Router.add[DigdagApi]

  val finagleDesign: Design = newFinagleServerDesign(port = serverPort, router = router)

  val curDirPath = Paths.get("")
  val tmpDirPath = Files.createTempDirectory(curDirPath, "test_temp_")
  val projDir = Files.createTempDirectory(tmpDirPath, "project1_")
  val dig1 = writeFile(projDir.resolve("test1.dig"),
    """
      |+t1:
      |  echo>: test1
      |""".stripMargin)
  val sql1 = writeFile(projDir.resolve("sql").resolve("query1.sql"),
    """
      |select count(*) from www_access
      |""".stripMargin)
  val projFiles = Seq(dig1, sql1).map(_.toFile)

  def runTest(testCode: (Design, Path, Path, Path, Seq[File]) => Unit): Unit = {
    try {
      testCode(finagleDesign, curDirPath, tmpDirPath, projDir, projFiles)
    }
    finally {
      FileUtils.deleteDirectory(tmpDirPath.toFile)
    }
  }

  @Endpoint(path = "/api")
  trait DigdagApi {
    @Endpoint(method = HttpMethod.GET, path = "/projects")
    def getPorjects(): Response = {
      val response = Response()
      response.contentString =
        s"""
           |{
           |  "projects": [
           |    {
           |      "id": "1",
           |      "name": "prj1",
           |      "revision": "f4924673-2e31-4582-90c6-bec8f21b4680",
           |      "createdAt": "2019-05-02T15:10:44Z",
           |      "updatedAt": "2019-05-02T15:11:31Z",
           |      "deletedAt": null,
           |      "archiveType": "db",
           |      "archiveMd5": "HxSzgWODvdFvHhCFR/nV4w=="
           |    },
           |    {
           |      "id": "2",
           |      "name": "prj2",
           |      "revision": "5e8cfbd8-73d9-4de5-84e5-7cb781c82551",
           |      "createdAt": "2019-05-02T15:12:29Z",
           |      "updatedAt": "2019-05-02T15:12:29Z",
           |      "deletedAt": null,
           |      "archiveType": "db",
           |      "archiveMd5": "pj7itvGXUqGvtK4P8lhVqQ=="
           |    }
           |  ]
           |}
           |
           |""".stripMargin
      response.contentType = "application/json;charset=utf-8"
      response
    }

    /**
     * curl -v  -X PUT -H 'Content-Type: application/gzip' --data-binary '@./test1.zip'  http://localhost:55432/api/projects
     *
     */

    sealed case class PutProjectRequest(project: String, revision: String, schedule_from: Option[String] = None)

    @Endpoint(method = HttpMethod.PUT, path = "/projects")
    def putPorjects(req: HttpMessage.Request): Response = {
      val body = req.contentBytes
      val in = catching(classOf[IOException]) withTry new GZIPInputStream(new ByteArrayInputStream(body))
      val response = Response()
      (req.contentType, in) match {
        case (Some("application/gzip"), Success(in2)) => {
          response.status(Status.Accepted)
          response.contentString = contentString(
            req.query.getOrElse("project", "unknown"),
            req.query.getOrElse("revision", "unknown"))
        }
        case (_, Failure(e)) => {
          println(e.toString)
          response.status(Status.BadRequest)
        }
        case _ => response.status(Status.BadRequest)
      }

      def contentString(p: String, r: String) =
        s"""
           |    {
           |      "id": "1",
           |      "name": "${p}",
           |      "revision": "${r}",
           |      "createdAt": "2019-05-02T15:10:44Z",
           |      "updatedAt": "2019-05-02T15:11:31Z",
           |      "deletedAt": null,
           |      "archiveType": "db",
           |      "archiveMd5": "HxSzgWODvdFvHhCFR/nV4w=="
           |    }
           |""".stripMargin

      response.contentType = "application/json;charset=utf-8"
      response
    }

    @Endpoint(method = HttpMethod.PUT, path = "/projects/:id/secrets/:key")
    def putSecret(id:Long, key:String): Response = {
      val response = Response()
      id match {
        case 1 => response.status(Status.Accepted)
        case _ => response.status((Status.NotFound))
      }
      response
    }

    @Endpoint(method = HttpMethod.POST, path = "/attempts/:id/kill")
    def killAttempt(id: Long): Response = {
      println(s"killAttempt: ${id}")
      val response = Response()
      response.status(Status.Accepted)
      response
    }

    @Endpoint(method = HttpMethod.GET, path = "/test/:id/kill")
    def test1(id: Long): Response = {
      println(s"test ${id}")
      val response = Response()
      response.status(Status.BadRequest)
      response
    }
  }
}

