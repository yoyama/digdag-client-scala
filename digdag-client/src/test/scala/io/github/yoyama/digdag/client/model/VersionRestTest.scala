package io.github.yoyama.digdag.client.model

import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success, Try}

class VersionRestTest extends FlatSpec with Matchers {

  "Valid json" should "parsed" in {
    val data = """{"version":"0.9.42"}""".stripMargin
    val version: Try[VersionRest] = VersionRest.toVersion(data)
    version match {
      case Success(v) => {
        assert(v.version == "0.9.42")
      }
      case Failure(exception) => {
        println(exception)
        assert(false)
      }
    }
  }
}
