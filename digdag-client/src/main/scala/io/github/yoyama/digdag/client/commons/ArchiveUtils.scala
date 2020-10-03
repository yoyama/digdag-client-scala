package io.github.yoyama.digdag.client.commons

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Path, Paths}
import io.github.yoyama.digdag.client.commons.Helpers.TryHelper

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Using
import scala.util.matching.Regex

trait ArchiveUtils {

  def archiveProject(dstPath:Path, srcPath:Path, excludes:Seq[Regex] = Seq.empty): Future[File] = {
    for {
      srcFiles <- validateSrc(srcPath)
      dst <- validateDst(dstPath).map(_.toPath)
      tar <- createTar(dst, srcPath, srcFiles)
    } yield tar.toFile
  }

  def sysTempDir:Path =  Paths.get(System.getProperty("java.io.tmpdir"))

  private def validateSrc(srcPath:Path):Future[Seq[File]] = {
    Future {
      srcPath.toFile match {
        case f if f.isFile => throw new RuntimeException("src must be directory")
        case f => listRecur(srcPath.toFile)
      }
    }
  }

  private[commons] def validateDst(dstPath:Path):Future[File] = Future {
    dstPath.toFile match {
      case d if d.exists() => throw new RuntimeException(s"The file exists: ${d.toString()}")
      case d if d.createNewFile() => d
    }
  }


  private[commons] def listRecur(f:File): Seq[File] = {
    f match {
      case f if f.isFile => Seq(f)
      case f => f.listFiles().flatMap(f2 => listRecur(f2))
    }
  }

  private[commons] def createTar(tar:Path, srcPath:Path, files:Seq[File]): Future[Path] = Future {
    val t = Using.Manager { use =>
      val out = use(new GzipCompressorOutputStream(new FileOutputStream(tar.toFile().getPath())))
      val outTar = use (new TarArchiveOutputStream(out))
      val prefixDir = srcPath.toFile().getAbsolutePath().replaceAll("\\\\", "/")

      files.foreach { f =>
        val fn = f.getAbsolutePath()
          .replaceAll("\\\\", "/")
          .replaceFirst(prefixDir, "")
        outTar.putArchiveEntry( new TarArchiveEntry(f, fn))
        outTar.write(Files.readAllBytes(f.toPath))
        outTar.closeArchiveEntry()
      }

      outTar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
      tar
    }
    t.toFuture()
  }.flatMap(x => x)
}
