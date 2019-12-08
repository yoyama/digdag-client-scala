package io.github.yoyama.digdag.client.commons

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.http.{SimpleHttpClient}
import wvlet.airframe.http.HttpResponse

import scala.concurrent.Future
import scala.util.Try

object Helpers {

  implicit class TryHelper[T](t: Try[T]) {
    def toFuture(): Future[T] = Future.fromTry(t)
  }

  implicit class OptionHelper[T](t: Option[T]) {
    def toFuture(message:String): Future[T] = toFuture(new Throwable(message))

    def toFuture(e:Throwable): Future[T] = t match {
      case None => Future.failed(e)
      case Some(v) => Future.successful(v)
    }
  }

  implicit class EitherHelper[L,R](v: Either[L,R]) {
    def toFuture(): Future[R] = v match {
      case Right(r) => Future.successful(r)
      case Left(e) => Future.failed(new Throwable(e.toString))
    }
  }

  implicit class SimpleHttpClientHelper[RESP](cl:SimpleHttpClient) {

    def callGetToRest[T](apiPath:String, queries: Map[String,String], funcToRest:String=>Try[T]):Future[T] = {
      for {
        r1 <- cl.callGetString(apiPath, queries)
        r2 <- Future.fromTry(funcToRest(r1.body.get))
      } yield r2
    }
  }

  implicit class HttpResponseHelper[RESP](resp:HttpResponse[RESP]) {

    def toRest[T](funcToRest:String=>Try[T]):Future[T] = {
      for {
        r1 <- Future.fromTry(funcToRest(resp.contentString))
      } yield r1
    }
  }

}