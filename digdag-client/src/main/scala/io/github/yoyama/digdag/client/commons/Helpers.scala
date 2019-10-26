package io.github.yoyama.digdag.client.commons

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Helpers {

  implicit class TryHelper[T](t: Try[T]) {
    def toFuture(): Future[T] = Future.fromTry(t)
  }

}