package io.github.yoyama.digdag.client.api

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import io.github.yoyama.digdag.client.model.ProjectRest
import wvlet.airframe.http.finagle.FinagleServer

import scala.language.postfixOps

class ProjectApiTest extends FlatSpec with Matchers {

  def testListProject(): Unit = {
    new ApiDigdagServerMockFixture {
      finagleDesign.build[FinagleServer] { server =>
        Await.result(api.getProjects(), 60 seconds) match {
          case projects: List[ProjectRest] => {
            assert(projects.size == 2)
            assert(projects.head.name == "prj1")
            assert(projects.tail.head.revision == "5e8cfbd8-73d9-4de5-84e5-7cb781c82551")
          }
        }
        server.stop
      }
    }
  }
}


