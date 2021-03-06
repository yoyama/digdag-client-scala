package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.model.ProjectRest
import org.apache.commons.io.FileUtils
import wvlet.airframe.http.finagle.FinagleServer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import io.github.yoyama.digdag.client.commons.Helpers.FutureHelper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AttemptApiTest extends AnyFlatSpec with Matchers {

  "kill an attempt" should "works" in {
    new ApiDigdagServerFixture {
      finagleDesign.build[FinagleServer] { server =>
        try {
          attemptApi.killAttempt(100).syncTry(60 seconds) match {
            case Success(_) =>
            case Failure(e) => {
              fail(s"Failed to kill attempt: ${e.toString()}")
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
