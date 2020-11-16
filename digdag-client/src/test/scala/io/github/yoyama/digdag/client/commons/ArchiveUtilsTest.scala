package io.github.yoyama.digdag.client.commons

import java.io.File

import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ArchiveUtilsTest extends AnyFlatSpec with Matchers with IOUtils {

  def withFixture(testCode: (ArchiveUtils, Path, Path, Seq[File]) => Any):Unit = {
    val utils = new ArchiveUtils {}
    val curDirPath = Paths.get("")
    val tmpDirPath = Files.createTempDirectory(curDirPath, "test_temp_")
    val projDir = Files.createTempDirectory(tmpDirPath, "project1_")
    val dig1 = writeFile(projDir.resolve("test1.dig"),
      """
        |+t1:
        |  echo>: test1
        |""".stripMargin)
    val sql1 = writeFile(projDir.resolve("sql").resolve("query1.sql"),
      """
        |select count(*) from www_access
        |""".stripMargin)
    val projFiles = Seq(dig1, sql1).map(_.toFile)
    try {
      val paths = testCode(utils, tmpDirPath, projDir, projFiles)
    }
    finally {
      FileUtils.deleteDirectory(tmpDirPath.toFile)
    }
  }

  "createTar" should "works" in withFixture { (utils, tempDir, projDir, projFiles) =>
    val tarF = utils.createTar(tempDir.resolve("test1.tar.gz"), projDir, projFiles)
    tarF onComplete {
      case Success(tar) => println(tar.toString)
      case Failure(t) => fail(s"failed:$t")
    }
    Await.result(tarF, Inf)
  }

  "archiveProject" should "works" in withFixture { (utils, tempDir, projDir, projFiles) =>
    val archiveF = utils.archiveProject(tempDir.resolve("test1.tar.gz"), projDir)
    archiveF onComplete {
      case Success(tar) => println(tar.toString)
      case Failure(t) => fail(s"failed:$t")
    }
    Await.result(archiveF, Inf)
  }

}
