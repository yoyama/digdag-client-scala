package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.commons.Helpers.FutureHelper
import org.apache.commons.io.FileUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import wvlet.airframe.http.finagle.FinagleServer

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

class ScheduleApiTest extends AnyFlatSpec with Matchers {

  "get schedules" should "works" in {
    new ScheduleApiDigdagServerFixture {
      finagleDesign.build[FinagleServer] { server =>
        try {
          val ret = scheduleApi.getSchedules().syncTry(60 seconds) match {
            case Success(schedules) => {
              assert(schedules.size == 2)
            }
            case Failure(e) => {
              fail(s"Failed to get schedules: ${e.toString()}")
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
