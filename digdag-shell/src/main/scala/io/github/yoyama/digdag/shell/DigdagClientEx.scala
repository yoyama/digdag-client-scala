package io.github.yoyama.digdag.shell

import io.github.yoyama.digdag.client.{ConnectionConfig, DigdagClient}
import io.github.yoyama.digdag.client.http.{SimpleHttpClient, SimpleHttpClientScalaJ}
import io.github.yoyama.digdag.client.model.ProjectRest
import io.github.yoyama.digdag.shell

import scala.math.max
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

class DigdagClientEx(client:DigdagClient) extends TablePrint {
  var vertical:Boolean = false

  def version: Try[String] = {
    val v = client.version
    println(conv2String(v))
    v
  }

  def projects: Try[Seq[ProjectRest]] = printTableTry(client.projects(), vertical = vertical)
}

object  DigdagClientEx{
  def apply(client:DigdagClient): DigdagClientEx = {
    new DigdagClientEx(client)
  }

  def apply(connInfo:ConnectionConfig = ConnectionConfig.local): DigdagClientEx = {
    new DigdagClientEx(DigdagClient(new SimpleHttpClientScalaJ, connInfo))
  }

  def apply(httpClient: SimpleHttpClient, connInfo:ConnectionConfig): DigdagClientEx = {
    new DigdagClientEx(DigdagClient(httpClient, connInfo))
  }
}

import scala.reflect.runtime.universe._
trait TablePrint {
  def printTableTry[T : ClassTag: TypeTag ](seq:Try[Seq[T]], width:Int = 80, line:Boolean = true, vertical:Boolean = false):Try[Seq[T]] = {
    seq match {
      case Success(s) => printTable(s, width, line, vertical)
      case Failure(e) => println(s"ERROR: ${e.toString}")
    }
    seq
  }

  def printTable[T : ClassTag: TypeTag ](seq:Seq[T], width:Int = 80, line:Boolean = true, vertical:Boolean = false):Seq[T] = {
    val members = typeOf[T].members.collect {
      case m: TermSymbol if m.isVal || m.isVar => m
    }.toList.reverse
    val mirror = runtimeMirror(classTag[T].getClass.getClassLoader)

    val tbl = seq.map { s =>
      val ms = mirror.reflect(s)
      members.foldLeft(Seq.empty[String]) { (acc, v) => acc ++ Seq(conv2String(ms.reflectField(v).get)) }
    }
    val headers = members.map(_.name.toString)
    if (vertical)
      printTableVertical(headers, tbl, width)
    else
      printTableRow(headers, tbl, width)
    seq
  }

  protected def conv2String(v:Any):String = v match {
    case Some(v2) => v2.toString
    case None => ""
    case Right(v2) => v2.toString
    case Left(e) => "error"
    case _ => v.toString
  }

  def hline(w:Int):String = Seq.fill(w)("-").mkString("")

  def printTableRow(headers:Seq[String], tbl:Seq[Seq[String]], width:Int):Unit = {
    val leftBar = "| "
    val colMax: Seq[Int] = (tbl ++ Seq(headers)).foldLeft(Seq.fill(headers.size)(0)){ (acc, v) =>
      val vlen = v.map(_.length)
      vlen.zip(acc).map(x => max(x._1, x._2))
    }
    print(leftBar)
    headers.zip(colMax).foreach { h => printf(s"%-${h._2}s%s", h._1, leftBar) }
    println()
    print("|")
    headers.zip(colMax).foreach { h =>
      printf("%s%s", hline(h._2 + 1), "|")
    }
    println()
    tbl.foreach { r =>
      print(leftBar)
      r.zip(colMax).foreach { c => printf(s"%-${c._2}s%s", c._1, leftBar) }
      println()
    }
  }

  def printTableVertical(headers:Seq[String], tbl:Seq[Seq[String]], width:Int):Unit = {
    val headerWidth = headers.max(Ordering.by[String, Int](x => x.length)).length
    tbl.foreach { r =>
      r.zip(headers).foreach { c =>
        printf(s" %${headerWidth}s| %s\n", c._2, c._1)
      }
      println()
    }
  }
}