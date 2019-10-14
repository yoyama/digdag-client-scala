package io.github.yoyama.digdag.client.model

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import io.github.yoyama.digdag.client.model.AttemptRest.toJsArray
import play.api.libs.json._

import scala.util.{Failure, Success, Try}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.control.Exception.catching

trait ModelUtils {
  val dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][xxx][xx][X]")

  def toJsArray(v:JsValue):Try[JsArray] = v match {
    case v1:JsArray => Success(v1)
    case v2 => Failure(new Throwable("Invalid json type:" + v2.toString()))
  }

  def getAsList[T](response:String, fieldName:String)(implicit read:Reads[T]):Try[List[T]] = {
    for {
      parsed: JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      jsArray: JsArray <- toJsArray(parsed(fieldName))
      list <- getAsList(jsArray)(read)
    } yield list
  }

  def getAsList[T](jsonArray:JsArray)(implicit read:Reads[T]) :Try[List[T]] = {
    jsonArray.validate[List[T]] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
  }

  def getAsSingle[T](response:String)(implicit read:Reads[T]):Try[T] = {
    for {
      parsed:JsValue <- catching(classOf[Throwable]) withTry Json.parse(response)
      single <- getAsSingle[T](parsed)
    } yield single
  }

  def getAsSingle[T](json:JsValue)(implicit read:Reads[T]):Try[T] = {
    json.validate[T] match {
      case JsSuccess(v, p) => Success(v)
      case e:JsError => Failure(new Throwable(e.toString))
    }
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


  implicit val jsonOffsetDateTimeRead  = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] = {
      JsSuccess(OffsetDateTime.parse(json.as[String], dateTimeFormatter))
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
        (JsPath \ "id").read[Option[String]] and
        (JsPath \ "name").read[Option[String]]
    )(IdAndName.apply _)
}
