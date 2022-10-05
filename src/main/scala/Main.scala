import zio._
import zio.json._
import zhttp.http._
import zhttp.service.Server

case class CreateTodoForm(title: String)
object CreateTodoForm {
  implicit val zioJsonCodecForCreateTodoForm: zio.json.JsonCodec[CreateTodoForm] = zio.json.DeriveJsonCodec.gen
}





case class HttpServer(todoRepo: TodoRepository) {

  val httpApp = Http.collectZIO[Request] {
    case Method.GET -> !! / "hello" =>
      ZIO.succeed(Response.text("hello"))

    case Method.GET -> !! / "todo" / "list" =>
      todoRepo.findAll.map(list =>  Response.json(list.toJson))

    case req @ Method.POST -> !! / "todo" =>
      for {
        text <- req.body.asCharSeq
        form <- ZIO.from(text.fromJson[CreateTodoForm])
          .mapError(msg => new Exception(msg))

        newTodo <- todoRepo.create(form.title)
      } yield {
        Response.json(newTodo.toJson)
      }
  }
}

object HttpServer {
  val layer: ZLayer[TodoRepository, Nothing, HttpServer] =
    ZLayer.fromFunction(HttpServer.apply _)
}

object Main extends ZIOAppDefault {

  val prog: ZIO[HttpServer with AppConfig, Throwable, Unit] = for {
    server <- ZIO.service[HttpServer]
    config <- ZIO.service[AppConfig]
    _ <- Server.start(config.http.port, server.httpApp)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    prog.provide(
      TodoRepositoryInMemory.layer,
      HttpServer.layer,
      AppConfig.layer,
    )
}