package yoyama.digdag.client.rest.model

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait ModelUtils {
  def toJsArray(v:JsValue):Try[JsArray] = v match {
    case v1:JsArray => Success(v1)
    case v2 => Failure(new Throwable("Invalid json type:" + v2.toString()))
  }

  implicit val jsonStringRead  = new Reads[String] {
    override def reads(json: JsValue): JsResult[String] = {
      JsSuccess(Json.stringify(json))
    }
  }
}
