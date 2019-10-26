package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}
import io.github.yoyama.digdag.client.model.{ProjectRest, WorkflowRest}

import io.github.yoyama.digdag.client.commons.Helpers.TryHelper
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class WorkflowApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getWorkflows(lastId:Option[Long] = None, count:Option[Long] = None):Future[List[WorkflowRest]] = {
    val apiPath = srvInfo.apiEndPoint("/api/workflows")
    val queries = Map[String,Option[String]](
      "lastid" -> lastId.map(_.toString), "count" -> count.map(_.toString))
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get)) // String -> String
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- WorkflowRest.toWorkflows(r2).toFuture
    } yield r3
  }

  def getWorkflow(id:Long):Future[WorkflowRest] = {
    val apiPath = srvInfo.apiEndPoint(s"/api/workflows/${id}")
    for {
      r1 <- httpClient.callGet(apiPath)
      r2 <- r1.asString()
      r3 <- WorkflowRest.toWorkflow(r2).toFuture
    } yield r3
  }
}

