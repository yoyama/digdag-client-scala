package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.model.{AttemptRest, SessionRest}
import io.github.yoyama.digdag.client.http.SimpleHttpClient
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig

import scala.concurrent.{ExecutionContext, Future}

class SessionApi(httpClient: SimpleHttpClient, connConfig:ConnectionConfig)(implicit val ec:ExecutionContext) extends BasicApi(connConfig) {
  override def apiPathPart = "/api/sessions"

  def getSessions(lastId:Option[Long] = None, pageSize:Option[Long] = None):Future[List[SessionRest]] = {

    val queries =
      Seq(("last_id", lastId), ("page_size", pageSize))
        .filter(_._2.isDefined)                //remove None
        .map(x => (x._1, x._2.get.toString))   //convert Long to String
        .toMap                                 //Map[String,String]
    for {
      r1 <- httpClient.callGetString(apiPathBase, queries = queries, headers = headers())
      body <- r1.body.toFuture("no body")
      r3 <- SessionRest.toSessions(body).toFuture()
    } yield r3
  }

  def getSession(id:Long):Future[SessionRest] = {
    val apiPath = s"${apiPathBase}/${id}"
    for {
      r1 <- httpClient.callGetString(apiPath, headers = headers())
      body <- r1.body.toFuture("no body")
      r3 <- SessionRest.toSession(body).toFuture()
    } yield r3
  }

  def getAttempts(id:Long):Future[List[AttemptRest]] = {
    val apiPath = s"${apiPathBase}/${id}/attempts"
    for {
      r1 <- httpClient.callGetString(apiPath, headers = headers())
      body <- r1.body.toFuture("no body")
      r3 <- AttemptRest.toAttempts(body).toFuture()
    } yield r3
  }
}

