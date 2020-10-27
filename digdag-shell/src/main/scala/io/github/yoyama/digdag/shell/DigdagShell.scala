package io.github.yoyama.digdag.shell

import scala.tools.nsc.{Properties, Settings}
import scala.tools.nsc.interpreter.shell.{ILoop, ShellConfig}
import io.github.yoyama.digdag.client._
import io.github.yoyama.digdag.client.config.ConnectionConfig

import scala.sys.Prop
import scala.tools.nsc.interpreter.Results.Result

object DigdagShell extends App {
  val settings = new Settings
  settings.usejavacp.value = true
  settings.deprecation.value = true
  val shellConfig = ShellConfig(settings)
  new DigdagILoop(shellConfig).run(settings)
}

class DigdagILoop(val shellConfig:ShellConfig) extends ILoop(shellConfig) {
  var conn:Option[ConnectionConfig] = None
  var connMap = Map("default" -> ConnectionConfig("default", "http://localhost:65432"))
  //override def prompt: String = conn.map(_.name).getOrElse("(no connect)") + "=> "
  //override lazy val prompt = "AAAAA>"

  import LoopCommand.cmd

  lazy val digdagCommands = Seq(
    cmd("connect", "", "connect to a digdag version", digdagConnect)
  )
  override def commands: List[LoopCommand] = super.commands ++ digdagCommands

  override def resetCommand(line: String): Unit = {
    super.resetCommand(line)
    initializeDigdag()
  }

  override def replay(): Unit = {
    initializeDigdag()
    super.replay()
  }

  def initializeDigdag(): Unit = {
    println("initializeDigdag called")
  }

  private def digdagConnect(line0: String): Result = {
    def split(s:String, regexp:String):List[String] = if (s == "") List.empty else s.split(regexp).toList
    val ret:String = split(line0, "\\s+") match {
      case Nil => conn.map(_.name).getOrElse("No connection")
      case "default" :: xs => {
        echoCommandMessage("default is set")
        intp.interpret("import io.github.yoyama.digdag.client.config.ConnectionConfig")
        intp.interpret("import io.github.yoyama.digdag.client.DigdagClient")
        intp.interpret("import io.github.yoyama.digdag.shell.{DigdagClientEx}")
        intp.interpret("""implicit val connectionConfig = ConnectionConfig("default", "http://localhost:65432") """)
        intp.interpret("""val dc = DigdagClient(connectionConfig)""")
        intp.interpret("""val dcx = DigdagClientEx(connectionConfig)""")
        ""
      }
      case _ => "Not yet implemented"
    }
    Result(true, Some(ret))
  }

  override def printWelcome(): Unit = {
    echo("\n" +
      "### Digdag Shell ###" +
      "\n")
  }
}

