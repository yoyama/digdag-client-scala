package io.github.yoyama.digdag.client.model

import scala.language.postfixOps
import play.api.libs.json._

import scala.util.{Failure, Success, Try}
import scala.util.control.Exception.catching


/**
 * {"version":"0.9.42"}
 */
case class VersionRest(version: String)

object VersionRest extends ModelUtils {
  implicit val versionReads: Reads[VersionRest] = new Reads[VersionRest]() {
    override def reads(json: JsValue): JsResult[VersionRest] = (json \ "version").validate[String].map(VersionRest(_))
  }

  def toVersion(response:String):Try[VersionRest] = {
    for {
      parsed:JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      version <- toVersion(parsed)
    } yield version
  }

  def toVersion(json:JsValue):Try[VersionRest] = {
    json.validate[VersionRest] match {
      case JsSuccess(v, _) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

}

