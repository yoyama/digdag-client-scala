package yoyama.digdag.client.rest.model

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import play.api.libs.json._

import scala.util.{Failure, Success, Try}
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait ModelUtils {
  def toJsArray(v:JsValue):Try[JsArray] = v match {
    case v1:JsArray => Success(v1)
    case v2 => Failure(new Throwable("Invalid json type:" + v2.toString()))
  }

  implicit val jsonStringRead  = new Reads[String] {
    override def reads(json: JsValue): JsResult[String] = {
      JsSuccess(json.as[String])
    }
  }

  implicit val jsonLongRead  = new Reads[Long] {
    override def reads(json: JsValue): JsResult[Long] = {
      JsSuccess(json.as[Long])
    }
  }


  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][xxx][xx][X]")
  implicit val jsonOffsetDateTimeRead  = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] = {
      JsSuccess(OffsetDateTime.parse(json.as[String], formatter))
    }
  }

  implicit val jsonOptionStringRead  = new Reads[Option[String]] {
    override def reads(json: JsValue): JsResult[Option[String]] = {
      JsSuccess(Option(json.as[String]))
    }
  }

  implicit val jsonOptionLongRead  = new Reads[Option[Long]] {
    override def reads(json: JsValue): JsResult[Option[Long]] = {
      JsSuccess(Option(json.as[Long]))
    }
  }

  implicit val jsonIdAndNameRead: Reads[IdAndName] = (
        (JsPath \ "id").read[Option[Long]] and
        (JsPath \ "name").read[Option[String]]
    )(IdAndName.apply _)
}
