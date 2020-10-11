package io.github.yoyama.digdag.client.api

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import io.github.yoyama.digdag.client.model.ProjectRest
import org.apache.commons.io.FileUtils
import wvlet.airframe.http.finagle.FinagleServer

import scala.language.postfixOps

class ProjectApiTest extends FlatSpec with Matchers {

  "getProjects" should "works" in {
    new ApiDigdagServerMockFixture {
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
}


