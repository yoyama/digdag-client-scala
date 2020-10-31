package io.github.yoyama.digdag.client.config

import io.github.yoyama.digdag.client.commons.IOUtils
import org.scalatest.{FlatSpec, Matchers}

class ConnectionConfigTest extends FlatSpec with Matchers with IOUtils {

  "loadAll" should "works" in {
    val configs = ConnectionConfig.loadAll()
    println(configs)
  }
}
