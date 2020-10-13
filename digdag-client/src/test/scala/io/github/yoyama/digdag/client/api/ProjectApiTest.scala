package io.github.yoyama.digdag.client.api

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import io.github.yoyama.digdag.client.model.ProjectRest
import io.github.yoyama.digdag.client.commons.Helpers.FutureHelper
import io.github.yoyama.digdag.client.http.SimpleHttpException
import org.apache.commons.io.FileUtils
import wvlet.airframe.http.finagle.FinagleServer

import scala.language.postfixOps
import scala.util.{Failure, Success}

class ProjectApiTest extends FlatSpec with Matchers {

  "getProjects" should "works" in {
    new ApiDigdagServerMockFixture {
      override def serverPort = 15431
      finagleDesign.build[FinagleServer] { server =>
        try {
          Await.result(projectApi.getProjects(), 60 seconds) match {
            case projects: List[ProjectRest] => {
              assert(projects.size == 2)
              assert(projects.head.name == "prj1")
              assert(projects.tail.head.revision == "5e8cfbd8-73d9-4de5-84e5-7cb781c82551")
            }
            case x => fail(s"Not list of ProjectRest: ${x.toString()}")
          }
        }
        finally {
          server.stop
          FileUtils.deleteDirectory(tmpDirPath.toFile)
        }
      }
    }
  }

  "pushProject" should "works" in {
    new ApiDigdagServerMockFixture {
      override def serverPort = 15432
      finagleDesign.build[FinagleServer] { server =>
        try {
          Await.result(projectApi.pushProjectDir("projectA", "revisionA", projDir), 60 seconds) match {
            case projects: ProjectRest => {
              assert(projects.name == "projectA")
              assert(projects.revision == "revisionA")
            }
            case x => fail(s"Not ProjectRest: ${x.toString()}")
          }
        }
        finally {
          server.stop
          FileUtils.deleteDirectory(tmpDirPath.toFile)
        }
      }
    }
  }

  "putSecret" should "works" in {
    new ApiDigdagServerMockFixture {
      override def serverPort = 15433
      finagleDesign.build[FinagleServer] { server =>
        try {
          projectApi.putSecret(1, "key1", "value1").syncTry(60 seconds) match {
            case Success(_) => //OK
            case Failure(e) => {
              e.printStackTrace()
              fail(e.toString)
            }
          }
          projectApi.putSecret(99, "key1", "value1").syncTry(60 seconds) match {
            case Success(_) => fail("Should fail")
            case Failure(SimpleHttpException(resp)) => {
              resp.statusCode match {
                case Some(404) => //OK
                case x => fail(s"Unexpected status code:${x}")
              }
            }
          }
        }
        finally {
          server.stop
          FileUtils.deleteDirectory(tmpDirPath.toFile)
        }
      }
    }
  }
}


