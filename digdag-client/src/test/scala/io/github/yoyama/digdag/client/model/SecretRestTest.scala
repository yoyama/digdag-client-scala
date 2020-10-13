package io.github.yoyama.digdag.client.model

import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success, Try}

class SecretRestTest extends FlatSpec with Matchers {

  "Valid json with keys" should "parsed" in {
    val data = """{
                 |  "secrets": [
                 |    {
                 |      "key": "s1"
                 |    },
                 |    {
                 |      "key": "s2"
                 |    }
                 |  ]
                 |}
    """.stripMargin
    val secrets: Try[SecretKeysRest] = SecretKeysRest.toSecretKeysRest(data)
    secrets match {
      case Success(a) => {
        assert(a.keys.size == 2)
        assert(a.keys.head == "s1")
        assert(a.keys.tail.head == "s2")
      }
      case Failure(exception) => {
        println(exception)
        assert(false)
      }
    }
  }

  "Valid json with no key" should "parsed" in {
    val data = """{
                 |  "secrets": [
                 |  ]
                 |}
    """.stripMargin
    val secrets: Try[SecretKeysRest] = SecretKeysRest.toSecretKeysRest(data)
    secrets match {
      case Success(a) => {
        assert(a.keys.size == 0)
      }
      case Failure(exception) => {
        println(exception)
        assert(false)
      }
    }
  }

}
