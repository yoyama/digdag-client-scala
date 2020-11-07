package io.github.yoyama.digdag.shell

import ammonite.interp.Watchable
import ammonite.util.Res

import scala.tools.nsc.{Properties, Settings}
import scala.tools.nsc.interpreter.shell.{ILoop, ShellConfig}
import io.github.yoyama.digdag.client._
import io.github.yoyama.digdag.client.config.ConnectionConfig

import scala.sys.Prop
import scala.tools.nsc.interpreter.Results.Result

object DigdagShellAmm extends App {
  var connMap = ConnectionConfig.loadAll().get
  var connName:Option[String] = None

  val amm = ammonite.Main(
    predefCode =
      """
        |import io.github.yoyama.digdag.client.config.ConnectionConfig
        |import io.github.yoyama.digdag.client.DigdagClient
        |import io.github.yoyama.digdag.shell.{DigdagClientEx, DigdagShellAmm => amm}
        |implicit val connectionConfig = amm.connMap.getOrElse(amm.connName.getOrElse("digdag"), null)
        |val dc = DigdagClient(connectionConfig)
        |val dcx = DigdagClientEx(dc)
        |
        |println("Starting DigdagShell")
        |""".stripMargin
  )
  amm.run(
    "fooValue" -> foo(),
    "connect" -> digdagConnect(":connect default")
  )

  def connect(name:String):Unit = {
    connName = Option(name)
    amm.runCode(
      """
        |implicit val connectionConfig = amm.connMap.getOrElse(amm.connName.getOrElse("digdag"), null)
        |val dc = DigdagClient(connectionConfig)
        |val dcx = DigdagClientEx(dc)
        |println(connectionConfig)
        |""".stripMargin)
  }
  def foo() = 1

  def digdagConnect(line0: String): Unit = {
    def split(s:String, regexp:String):List[String] = if (s == "") List.empty else s.split(regexp).toList
    val ret:Unit = split(line0, "\\s+") match {
      case Nil => "No connection"
      case conn :: xs => {
        /**
        echoCommandMessage(s"""${conn} is set""")
        intp.interpret(s"""implicit val connectionConfig = DigdagShell.connMap("${conn}")""")
        intp.interpret("""val dc = DigdagClient(connectionConfig)""")
        intp.interpret("""val dcx = DigdagClientEx(connectionConfig)""")
        */
      }
    }
  }
}


