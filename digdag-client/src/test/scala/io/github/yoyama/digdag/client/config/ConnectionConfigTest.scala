package io.github.yoyama.digdag.client.config

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

import io.github.yoyama.digdag.client.commons.IOUtils
import org.apache.commons.io.FileUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConnectionConfigTest extends AnyFlatSpec with Matchers with IOUtils {

  "loadAll(dir)" should "return empty" in {
    val configs = ConnectionConfig.loadAll(Paths.get("/tmp/non_existed_dir"))
    assert(configs.isSuccess)
    assert(configs.get.size == 0)
  }

  "loadAll(dir)" should "return configs" in {
    val curDirPath = Paths.get("")
    val tmpDirPath = Files.createTempDirectory(curDirPath, "test_config_base_")
    try {
      val configBasiDirPath = Files.createDirectories(tmpDirPath.resolve(".config").resolve("digdag"))
      Files.write(configBasiDirPath.resolve("config"),
        s"""
           |client.http.endpoint = http://my_digdag:12345
           |client.http.headers.authorization = Basic AAAAAA
           |client.http.headers.header1 = value1
           |client.http.headers.header2 = value2
           |""".stripMargin.getBytes(UTF_8))
      val configsT = ConnectionConfig.loadAll(configBasiDirPath)
      println(configsT)
      assert(configsT.isSuccess)
      val configs = configsT.get
      assert(configs.size == 1)
      val configDigdagO = configs.get("digdag")
      assert(configDigdagO.isDefined)
      val configDigdag = configDigdagO.get
      assert(configDigdag.name == "digdag")
      assert(configDigdag.endPoint.toString == "http://my_digdag:12345")
      assert(configDigdag.auth == AuthConfigRaw("Basic AAAAAA"))
      assert(configDigdag.headers.get("header1") == Some("value1"))
      assert(configDigdag.headers.get("header2") == Some("value2"))
    }
    finally {
      FileUtils.deleteDirectory(tmpDirPath.toFile)
    }
  }
}
