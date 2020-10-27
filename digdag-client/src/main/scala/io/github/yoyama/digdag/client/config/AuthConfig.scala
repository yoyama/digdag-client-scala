package io.github.yoyama.digdag.client.config

trait AuthConfig {
  def authHeader(): Option[String]
}

case class AuthConfigNone() extends AuthConfig {
  override def authHeader(): Option[String] = None
}

case class AuthConfigBasic(user:String, pass:String) extends AuthConfig {
  import java.util.Base64
  override def authHeader(): Option[String] = {
    val b = Base64.getEncoder.encode(s"${user}:${pass}".getBytes)
    Some(s"Basic ${new String(b)}")
  }
}

case class AuthConfigRaw(header:String) extends AuthConfig {
  override def authHeader(): Option[String] = Some(header)
}
