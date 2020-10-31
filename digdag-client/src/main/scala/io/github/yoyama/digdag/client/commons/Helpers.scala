package io.github.yoyama.digdag.client.commons

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.yoyama.digdag.client.http.SimpleHttpClient

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps

object Helpers {
  implicit class FutureHelper[T](f: Future[T]) {
    def syncTry(implicit wait: FiniteDuration): Try[T] = {
      val f2 = f.map(Success(_)).recover {
        case e:Throwable => Failure(e)
        case x => Failure(new Throwable(x))
      }
      Await.result(f2, wait)
    }
  }

  implicit class TryHelper[T](t: Try[T]) {
    def toFuture(): Future[T] = Future.fromTry(t)
  }

  implicit class OptionHelper[T](t: Option[T]) {
    def toFuture(message:String): Future[T] = toFuture(new Throwable(message))

    def toFuture(e:Throwable): Future[T] = t match {
      case None => Future.failed(e)
      case Some(v) => Future.successful(v)
    }

    def toTry(message:String): Try[T] = toTry(new Throwable(message))

    def toTry(e:Throwable): Try[T] = t match {
      case None => Failure(e)
      case Some(v) => Success(v)
    }
  }

  implicit class EitherHelper[L,R](v: Either[L,R]) {
    def toFuture(): Future[R] = v match {
      case Right(r) => Future.successful(r)
      case Left(e) => Future.failed(new Throwable(e.toString))
    }
  }

  implicit class SimpleHttpClientHelper[RESP](cl:SimpleHttpClient) {

    def callGetToRest[T](apiPath:String, funcToRest:String=>Try[T]
                         , queries: Map[String,String] = Map.empty, headers: Map[String,String] = Map.empty):Future[T] = {
      for {
        r1 <- cl.callGetString(apiPath, queries, headers)
        r2 <- Future.fromTry(funcToRest(r1.body.get))
      } yield r2
    }
  }

}