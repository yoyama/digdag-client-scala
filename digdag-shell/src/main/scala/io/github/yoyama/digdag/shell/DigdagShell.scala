package io.github.yoyama.digdag.shell

import scala.tools.nsc.{Properties, Settings}
import scala.tools.nsc.interpreter.shell.{ILoop, ShellConfig}
import io.github.yoyama.digdag.client._
import io.github.yoyama.digdag.client.config.ConnectionConfig

import scala.sys.Prop
import scala.tools.nsc.interpreter.Results.Result

object DigdagShell extends App {
  var connMap = ConnectionConfig.loadAll().get
  var connName:Option[String] = None

  val settings = new Settings
  settings.usejavacp.value = true
  settings.deprecation.value = true
  val shellConfig = ShellConfig(settings)
  new DigdagILoop(shellConfig).run(settings)

  def reloadConnMap():Unit = {
    connMap = ConnectionConfig.loadAll().get
  }
}

class DigdagILoop(val shellConfig:ShellConfig) extends ILoop(shellConfig) {
  import LoopCommand.cmd

  lazy val digdagCommands = Seq(
    cmd("connect", "", "connect to a digdag version", digdagConnect)
  )

  override def commands: List[LoopCommand] = super.commands ++ digdagCommands

  val initializationCommands: Seq[String] = Seq(
    "import io.github.yoyama.digdag.client.config.ConnectionConfig",
    "import io.github.yoyama.digdag.client.DigdagClient",
    "import io.github.yoyama.digdag.shell.{DigdagClientEx, DigdagShell}",
    """implicit val connectionConfig = DigdagShell.connMap.getOrElse(DigdagShell.connName.getOrElse("digdag"), null) """,
    """val dc = DigdagClient(connectionConfig)""",
    """val dcx = DigdagClientEx(dc)"""
  )

  override protected def internalReplAutorunCode(): Seq[String] = initializationCommands

  override def resetCommand(line: String): Unit = {
    super.resetCommand(line)
    initializeDigdag()
  }

  override def replay(): Unit = {
    super.replay()
  }

  def initializeDigdag(): Unit = {
    println("initializeDigdag called")
    DigdagShell.reloadConnMap()
    initializationCommands.foreach(intp quietRun _)
  }

  private def digdagConnect(line0: String): Result = {
    def split(s:String, regexp:String):List[String] = if (s == "") List.empty else s.split(regexp).toList
    val ret:String = split(line0, "\\s+") match {
      case Nil => "No connection"
      case conn :: xs => {
        echoCommandMessage(s"""${conn} is set""")
        intp.interpret(s"""implicit val connectionConfig = DigdagShell.connMap("${conn}")""")
        intp.interpret("""val dc = DigdagClient(connectionConfig)""")
        intp.interpret("""val dcx = DigdagClientEx(connectionConfig)""")
        ""
      }
    }
    Result(true, Some(ret))
  }

  override def printWelcome(): Unit = {
    echo("\n" +
      "### Digdag Shell ###" +
      "\n")
  }
}

