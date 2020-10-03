package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.ConnectionConfig
import io.github.yoyama.digdag.client.model.WorkflowRest
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, TryHelper}
import io.github.yoyama.digdag.client.http.SimpleHttpClient

import scala.concurrent.{ExecutionContext, Future}

class WorkflowApi(httpClient: SimpleHttpClient, srvInfo:ConnectionConfig)(implicit val ec:ExecutionContext){

  def getWorkflows(lastId:Option[Long] = None, count:Option[Long] = None):Future[List[WorkflowRest]] = {
    val apiPath = srvInfo.apiEndPoint("/api/workflows")
    val queries = Map[String,Option[String]](
      "lastid" -> lastId.map(_.toString), "count" -> count.map(_.toString))
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get)) // String -> String
    for {
      resp <- httpClient.callGetString(apiPath)
      body <- resp.body.toFuture("No body data")
      rest <- WorkflowRest.toWorkflows(body).toFuture()
    } yield rest
  }

  def getWorkflow(id:Long):Future[WorkflowRest] = {
    val apiPath = srvInfo.apiEndPoint(s"/api/workflows/${id}")
    for {
      resp <- httpClient.callGetString(apiPath)
      body <- resp.body.toFuture("No body data")
      rest <- WorkflowRest.toWorkflow(body).toFuture()
    } yield rest
  }
}

