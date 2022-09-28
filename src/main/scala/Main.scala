import zio._
import zio.json._
import zhttp.http._
import zhttp.service.Server

object Main extends ZIOAppDefault {

  val todoList = List(
    Todo(1, "scala study"),
    Todo(2, "ZIO study")
  )

  val httpApp = Http.collectZIO[Request] {
    case Method.GET -> !! / "hello" =>
      ZIO.succeed(Response.text("hello"))

    case Method.GET -> !! / "todo" / "list" =>
      ZIO.succeed(Response.json(
        todoList.toJson
      ))
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Server.start(8080, httpApp)
}