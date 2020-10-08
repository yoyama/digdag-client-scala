package io.github.yoyama.digdag.client.commons

import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import com.github.benmanes.caffeine.cache.Async
import org.apache.commons.io.{FileUtils, FilenameUtils}

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


class ArchiveUtilsTest extends FlatSpec with Matchers {
  //Files.write(file, lines, StandardCharsets.UTF_8)
  def writeFile(path:Path, content:String):Path = {
    path.getParent().toFile.mkdirs()
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))
    path
  }

  def withFixture(testCode: (ArchiveUtils, Path, Path, Seq[File]) => Any) {
    val utils = new ArchiveUtils {}
    val curDirPath = Paths.get("")
    val tmpDirPath = Files.createTempDirectory(curDirPath, "test_temp_")
    val projDir = Files.createTempDirectory(tmpDirPath, "project1_")
    val dig1 = writeFile(projDir.resolve("test1.dig"),
      """
        |+t1:
        |  echo>: test1
        |""".stripMargin)
    val dig2 = writeFile(projDir.resolve("sql").resolve("query1.sql"),
      """
        |select count(*) from www_access
        |""".stripMargin)
    val projFiles = Seq(dig1, dig2).map(_.toFile)
    try {
      val paths = testCode(utils, tmpDirPath, projDir, projFiles)
    }
    finally {
      FileUtils.deleteDirectory(tmpDirPath.toFile)
    }
  }

  "createTar" should "works" in withFixture { (utils, tempDir, projDir, projFiles) =>
    val tarF = utils.createTar(tempDir.resolve("test1.tar"), projDir, projFiles)
    tarF onComplete {
      case Success(tar) => println(tar.toString)
      case Failure(t) => fail(s"failed:$t")
    }
    Await.result(tarF, Inf)
  }
}
