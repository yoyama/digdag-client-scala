package io.github.yoyama.digdag.shell

import java.util.Date

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class DigdagClientExTest {

}

sealed case class Data(no:Int, name:String, date:Date, description:Option[String] = None)

class TablePrintTest  extends AnyFlatSpec with Matchers {
  sealed trait Fixture {
    val data = Seq(
      Data(1, "Test Taro", new Date()),
      Data(2, "Test Jiro", new Date()),
      Data(3, "Test Subrooooooooooooooo", new Date(), Some("aaaaaaaaaaaaaaaaaaaaaa")),
    )
    val tp = new TablePrint {

    }
  }

  "printTable" should "work" in {
    new Fixture {
      tp.printTable(data)
      tp.printTable(data, vertical = true)
    }
  }
}