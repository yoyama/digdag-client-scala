package io.github.yoyama.digdag.shell

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.ILoop

object DigdagShell extends App {
  val settings = new Settings
  settings.usejavacp.value = true
  settings.deprecation.value = true
  new DigdagILoop().process(settings)
}

class DigdagILoop extends ILoop {
  override def prompt = "(no connect)=> "

  import LoopCommand.cmd

  lazy val digdagCommands = Seq(
    cmd("version", "", "show digdag version", digdagVersion)
  )
  override def commands: List[LoopCommand] = super.commands ++ digdagCommands

  /**
  addThunk {
    intp.beQuietDuring {
      intp.addImports("java.lang.Math._")
    }
  }
  */

  private def digdagVersion(line0: String): Result = {
    "0.9.38"
  }
  override def printWelcome() {
    echo("\n" +
      "Digdag Shell" +
      "\n")
  }
}

