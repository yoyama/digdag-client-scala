package io.github.yoyama.digdag.client.model


import java.time.Instant

import play.api.libs.json.{JsArray, JsError, JsObject, JsPath, JsResult, JsSuccess, JsValue, Json, Reads}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.util.control.Exception.catching

/**
 * {"secrets":[{"key":"s1"},{"key":"s2"}]}
 *
 */

case class SecretKeysRest(keys:Seq[String])

object SecretKeysRest extends ModelUtils {
  case class Key(k:String, v:String)

  implicit val keyRead: Reads[Key] = new Reads[Key] {
    def reads(json: JsValue): JsResult[Key] = {
      val v: Option[JsValue] = json.as[JsObject].value.get("key")
      v match {
        case Some(x) => JsSuccess(Key("key", x.as[String]))
        case _ => JsError(s"Invalid scret key format: ${json.toString()}")
      }
    }
  }

  def toSecretKeysRest(response:String):Try[SecretKeysRest] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      lists: JsArray <- toJsArray(parsed("secrets"))
      keys <- toKeys(lists)
      rest <- toSecretKeysRest(keys)
    } yield rest
  }

  def toKeys(jsonArray:JsArray): Try[Seq[Key]] = {
    jsonArray.validate[Seq[Key]] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

  def toSecretKeysRest(keys:Seq[Key]):Try[SecretKeysRest] = {
    Try(SecretKeysRest(keys = keys.map(_.v)))
  }
}




