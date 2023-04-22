import io.netty.util.internal.logging.{InternalLoggerFactory, Slf4JLoggerFactory}
import org.slf4j.impl.{StaticLoggerBinder, ZioLoggerFactory}
import zio._
import zio.config.typesafe.TypesafeConfigProvider
import zio.json._
import zio.http._
import zio.http.netty.BodyExtensions
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

  override val bootstrap =
    Runtime.setConfigProvider(
      TypesafeConfigProvider.fromResourcePath().kebabCase
    ) >>>
    Runtime.removeDefaultLoggers >>>
      zio.logging.consoleLogger() >>>
      Slf4jBridge.initialize

  val prog: ZIO[Server with HttpServer, Throwable, Unit] = for {
    _ <- ZIO.logInfo("Loading HTTP server...")
    server <- ZIO.service[HttpServer]
    _ <- ZIO.logInfo("Loading AppConfig...")

    port <- Server.install(server.httpApp.withDefaultErrorResponse)
    _ <- ZIO.logInfo(s"HTTP server started at port $port")
    _ <- ZIO.never
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    prog.provideSome[Scope](
      TodoRepositoryInMemory.layer,
      HttpServer.layer,
      Server.configured("zio-study.http")
    )
}