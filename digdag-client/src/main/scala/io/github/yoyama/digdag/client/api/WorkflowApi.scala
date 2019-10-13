package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.{DigdagServerInfo, HttpClientAkka}
import io.github.yoyama.digdag.client.model.{ProjectRest, WorkflowRest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class WorkflowApi(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getWorkflows(lastId:Option[Long] = None, count:Option[Long] = None):Future[Try[List[WorkflowRest]]] = {
    val apiPath = srvInfo.apiEndPoint("/api/workflows")
    val queries = Map[String,Option[String]](
      "lastid" -> lastId.map(_.toString), "count" -> count.map(_.toString))
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get)) // String -> String
    val responseF = httpClient.callGet(apiPath, queries)
    responseF.flatMap(_.asString()).map(WorkflowRest.toWorkflows(_))
  }

  def getWorkflow(id:Long):Future[Try[WorkflowRest]] = {
    val apiPath = srvInfo.apiEndPoint(s"/api/workflows/${id}")
    val responseF = httpClient.callGet(apiPath)
    responseF.flatMap(_.asString()).map(x => {
      println(x);
      WorkflowRest.toWorkflow(x)
    })
  }
}

