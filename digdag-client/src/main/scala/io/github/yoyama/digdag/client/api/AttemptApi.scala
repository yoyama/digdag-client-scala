package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.model.{AttemptRest, SessionRest}
import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class AttemptApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  val apiPathPart = "/api/attempts"
  def getAttempts(prjName:String, wfName:String, includeRetried:Boolean = false, lastId:Option[Long] = None,
                  pageSize:Option[Long] = None):Future[Try[List[AttemptRest]]] = {
    val queriesPart: Map[String, String] =
      Seq(("last_id", lastId), ("page_size", pageSize))
        .filter(_._2.isDefined)            //remove None
        .map(x => (x._1, x._2.toString))   //convert Long to String
        .toMap                             //Map[String,String]
    val queries = queriesPart + (("include_retried", includeRetried.toString))
    val apiPath = srvInfo.endPoint.toString + apiPathPart
    val responseF = httpClient.callGet(apiPath, queries = queries)
    responseF.flatMap(_.asString()).map(AttemptRest.toAttempts(_))
  }

  def getAttempt(id:Long):Future[Try[AttemptRest]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(AttemptRest.toAttempt(_))
  }

  def getAttemptRetries(id:Long):Future[Try[List[AttemptRest]]] = {
    val apiPath = srvInfo.endPoint.toString + s"${apiPathPart}/${id}/retries"
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(AttemptRest.toAttempts(_))
  }

  //def getTasks

  //def startAttempt


}

