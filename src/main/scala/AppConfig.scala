import zio._
import zio.config._
import zio.config.derivation.{describe, name}
import zio.config.typesafe.TypesafeConfigSource

@describe("config of the whole application")
case class AppConfig(
    @describe("config for http server")
    http: HttpConfig
)

object AppConfig {

  val descriptor: ConfigDescriptor[AppConfig] = zio.config.magnolia.descriptor

  val layer = ZLayer {
    read(descriptor from TypesafeConfigSource.fromResourcePath.at(path"zio-study"))
  }
}

@name("HTTP Module")
case class HttpConfig(
  @describe("http port. 0이면 가능한 아무 포트나 잡아준다")
  port: Int
)

case class DBConfig()