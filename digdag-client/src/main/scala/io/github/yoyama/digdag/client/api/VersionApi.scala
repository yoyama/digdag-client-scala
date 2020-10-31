package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.commons.Helpers.{OptionHelper, TryHelper}
import io.github.yoyama.digdag.client.config.ConnectionConfig
import io.github.yoyama.digdag.client.http.SimpleHttpClient
import io.github.yoyama.digdag.client.model.VersionRest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VersionApi(httpClient: SimpleHttpClient, connConfig:ConnectionConfig) extends BasicApi(connConfig) {

  override def apiPathPart: String = "/api/version"

  def getVersion():Future[VersionRest] = {
    for {
      r1 <- httpClient.callGetString(apiPathBase, headers = headers())
      body <- r1.body.toFuture("no body")
      r3 <- VersionRest.toVersion(body).toFuture()
    } yield r3
  }
}

