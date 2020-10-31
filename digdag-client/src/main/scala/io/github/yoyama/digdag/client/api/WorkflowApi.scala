package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.model.WorkflowRest
import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig
import io.github.yoyama.digdag.client.http.SimpleHttpClient

import scala.concurrent.{ExecutionContext, Future}

class WorkflowApi(httpClient: SimpleHttpClient, connConfig:ConnectionConfig)(implicit val ec:ExecutionContext)
              extends BasicApi(connConfig) {

  override def apiPathPart = "/api/workflows"

  def getWorkflows(lastId:Option[Long] = None, count:Option[Long] = None):Future[List[WorkflowRest]] = {
    val queries = Map[String,Option[String]](
      "lastid" -> lastId.map(_.toString), "count" -> count.map(_.toString))
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get)) // String -> String
    for {
      resp <- httpClient.callGetString(apiPathBase, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- WorkflowRest.toWorkflows(body).toFuture()
    } yield rest
  }

  def getWorkflow(id:Long):Future[WorkflowRest] = {
    val apiPath = s"${apiPathBase}/${id}"
    for {
      resp <- httpClient.callGetString(apiPath, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- WorkflowRest.toWorkflow(body).toFuture()
    } yield rest
  }
}

