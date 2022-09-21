import zio._
import zhttp.http._
import zhttp.service.Server

object Main extends ZIOAppDefault {

  val httpApp = Http.collectZIO[Request] {
    case Method.GET -> !! / "hello" =>
      ZIO.succeed(Response.text("hello"))
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Server.start(8080, httpApp)
}