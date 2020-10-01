package io.github.yoyama.digdag.client.model

case class IdAndName(id:Option[String], name:Option[String]) {
  override def toString: String = {
    id.getOrElse("") + "," + name.getOrElse("")
  }
}
