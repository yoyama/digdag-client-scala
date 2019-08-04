package yoyama.digdag.client.rest.api

import yoyama.digdag.client.DigdagServerInfo
import yoyama.digdag.client.http.HttpClientAkka
import yoyama.digdag.client.rest.model.{ProjectRest, WorkflowRest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class WorkflowAPI(httpClient: HttpClientAkka, srvInfo:DigdagServerInfo){

  def getWorkflows(lastId:Option[Long] = None, count:Option[Long] = None):Future[Try[List[WorkflowRest]]] = {
    val apiPath = srvInfo.apiEndPoint("/api/workflows")
    val queries = Map[String,Option[String]](
      "lastid" -> lastId.map(_.toString), "count" -> count.map(_.toString))
      .filter(x => x._2.nonEmpty)
      .map(x => (x._1, x._2.get)) // String -> String
    val responseF = httpClient.callGet(apiPath, queries)
    responseF.flatMap(_.asString()).map(WorkflowRest.toWorkflows(_))
  }
}

