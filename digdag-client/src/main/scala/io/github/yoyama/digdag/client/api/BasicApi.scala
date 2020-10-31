package io.github.yoyama.digdag.client.api

import io.github.yoyama.digdag.client.config.ConnectionConfig
import wvlet.log.LogSupport

class BasicApi(connConfig:ConnectionConfig) extends LogSupport{
  def headers(): Map[String, String] = connConfig.auth.authHeader().map(v => Map("Authorization" -> v)).getOrElse(Map.empty)
  def apiPathPart:String = ""
  def apiPathBase:String = connConfig.endPoint.toString + apiPathPart

}
