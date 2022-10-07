import io.netty.util.internal.logging.{InternalLoggerFactory, Slf4JLoggerFactory}
import org.slf4j.impl.{StaticLoggerBinder, ZioLoggerFactory}
import zio._
import zio.json._
import zhttp.http._
import zhttp.service.Server
import zio.logging.slf4j.bridge.Slf4jBridge

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

  val logging =
    Runtime.removeDefaultLoggers >>>
      zio.logging.console(logLevel = LogLevel.All) >>>
      Slf4jBridge.initialize

  val prog: ZIO[HttpServer with AppConfig, Throwable, Unit] = for {
    _ <- ZIO.logInfo("Loading HTTP server...")
    server <- ZIO.service[HttpServer]
    _ <- ZIO.logInfo("Loading AppConfig...")
    config <- ZIO.service[AppConfig]

    _ <- ZIO.logInfo(s"Starting HTTP Server at port ${config.http.port}...")
    _ <- Server.start(config.http.port, server.httpApp)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    prog.provide(
      logging,
      TodoRepositoryInMemory.layer,
      HttpServer.layer,
      AppConfig.layer,
    )
}