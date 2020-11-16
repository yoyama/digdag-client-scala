package io.github.yoyama.digdag.client.model


import scala.util.{Failure, Success, Try}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class VersionRestTest extends AnyFlatSpec with Matchers {

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
