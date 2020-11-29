package io.github.yoyama.digdag.client.api

import java.time.{Instant, OffsetDateTime}

import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig
import io.github.yoyama.digdag.client.http.{SimpleHttpClient, SimpleHttpResponse}
import io.github.yoyama.digdag.client.model.request.{AttemptRequestRest, ScheduleBackfillRequestRest, ScheduleSkipRequestRest}
import io.github.yoyama.digdag.client.model.{AttemptRest, ScheduleRest, TaskRest}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future}


class ScheduleApi(httpClient: SimpleHttpClient, connConfig:ConnectionConfig)(implicit val ec:ExecutionContext)
              extends BasicApi(connConfig) {
  override def apiPathPart = "/api/schedules"

  def getSchedules(lastId: Option[Long] = None): Future[List[ScheduleRest]] = {
    def procQueries(): Map[String, String] = {
      val queriesPart: Map[String, String] =
        Seq(("last_id", lastId))
          .filter(_._2.isDefined) //remove None
          .map(x => (x._1, x._2.get.toString)) //convert Long to String
          .toMap //Map[String,String]
      queriesPart
    }

    for {
      resp <- httpClient.callGetString(apiPathBase, queries = procQueries(), headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- ScheduleRest.toSchedules(body).toFuture()
    } yield rest
  }

  def getSchedule(id: Long): Future[ScheduleRest] = {
    val apiPath = s"${apiPathBase}/${id}"
    for {
      resp <- httpClient.callGetString(apiPath, queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      rest <- ScheduleRest.toSchedule(body).toFuture()
    } yield rest
  }

  def skip(id: Long, count:Option[Long] = None, fromTime:Option[Instant] = None, nextTime:Option[OffsetDateTime] = None,
           nextRunTime:Option[Instant] = None, dryRun:Boolean = false): Future[ScheduleRest] = {
    val apiPath = s"${apiPathBase}/${id}/skip"
    val sreq = ScheduleSkipRequestRest(count, fromTime, nextTime, nextRunTime, dryRun)
    val ret = for {
      resp <- httpClient.callPostString(apiPath, "application/json", Json.toJson(sreq).toString()
                , queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      data <- ScheduleRest.toSchedule(body).toFuture()
    } yield data
    ret
  }

  def backfill(id: Long, fromTime:Instant, attemptName:String, dryRun:Boolean = false, count:Option[Long] = None): Future[List[AttemptRest]] = {
    val apiPath = s"${apiPathBase}/${id}/backfill"
    val sreq = ScheduleBackfillRequestRest(fromTime, dryRun, attemptName, count)
    val ret = for {
      resp <- httpClient.callPostString(apiPath, "application/json", Json.toJson(sreq).toString()
        , queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      data <- AttemptRest.toAttemptsFromBackfill(body).toFuture()
    } yield data
    ret
  }

  def disable(id: Long): Future[ScheduleRest] = disableEnable(id, false)
  def enable(id: Long): Future[ScheduleRest] = disableEnable(id, true)

  private def disableEnable(id: Long, enable:Boolean): Future[ScheduleRest] = {
    val verb = if (enable) "enable" else "disable"
    val apiPath = s"${apiPathBase}/${id}/${verb}"
    val ret = for {
      resp <- httpClient.callPostString(apiPath, "application/json", null, queries = Map.empty, headers = headers())
      body <- resp.body.toFuture("No body data")
      data <- ScheduleRest.toSchedule(body).toFuture()
    } yield data
    ret
  }
}

