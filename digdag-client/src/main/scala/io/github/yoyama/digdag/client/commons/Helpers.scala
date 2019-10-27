package io.github.yoyama.digdag.client.commons

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.http.HttpClientDigdag
import wvlet.airframe.http.HttpResponse

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Helpers {

  implicit class TryHelper[T](t: Try[T]) {
    def toFuture(): Future[T] = Future.fromTry(t)
  }

  implicit class HttpClientDigdagHelper[REQ, RES](cl:HttpClientDigdag[REQ,RES]) {

    def callGetToRest[T](apiPath:String, queries: Map[String,String], funcToRest:String=>Try[T]):Future[T] = {
      import io.github.yoyama.digdag.client.commons.Helpers.TryHelper
      for {
        r1 <- cl.callGet(apiPath, queries)
        r2 <- Future.fromTry(funcToRest(r1.contentString))
      } yield r2
    }
  }
}