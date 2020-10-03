package io.github.yoyama.digdag.client.commons

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

trait IOUtils {
  def writeFile(path:Path, content:String):Path = {
    path.getParent().toFile.mkdirs()
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))
    path
  }
}
