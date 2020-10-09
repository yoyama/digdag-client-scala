package io.github.yoyama.digdag.client.api

import java.io.{ByteArrayInputStream, IOException}
import java.nio.file.{Files, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import com.twitter.finagle.http.{Response, Status}
import io.github.yoyama.digdag.client.ConnectionConfig
import io.github.yoyama.digdag.client.http.SimpleHttpClientScalaJ
import wvlet.airframe.Design
import wvlet.airframe.http.{HttpMessage, HttpRequest, Router}
import java.util.zip.GZIPInputStream

import io.github.yoyama.digdag.client.commons.IOUtils

import scala.util.{Failure, Success}
import scala.util.control.Exception._

private[api] trait ApiDigdagServerMockFixture extends IOUtils {

  import wvlet.airframe.http.finagle._
  import wvlet.airframe.http.{Endpoint, HttpMethod}

  val serverPort = 55432
  val endPointURL = s"http://localhost:${serverPort}/api"
  val connInfo = ConnectionConfig("test", s"http://localhost:${serverPort}")
  val httpClient = new SimpleHttpClientScalaJ()
  val api = new ProjectApi(httpClient, connInfo)

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

    sealed case class PutProjectRequest(project:String, revision:String, schedule_from:Option[String] = None)

    @Endpoint(method = HttpMethod.PUT, path = "/projects")
    def putPorjects(req:HttpMessage.Request): Response = {
      val body = req.contentBytes
      println(s"YY body length: ${body.length}")
      //println(new String(body))
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
      def contentString(p:String, r:String) =
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
  }

}
