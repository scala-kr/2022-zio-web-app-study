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





object HttpServer {

  val httpApp = Http.collectZIO[Request] {
    case Method.GET -> !! / "hello" =>
      ZIO.succeed(Response.text("hello"))

    case Method.GET -> !! / "todo" / "list" =>
      TodoRepository.findAll.map(list =>  Response.json(list.toJson))

    case req @ Method.POST -> !! / "todo" =>
      for {
        text <- req.body.asCharSeq
        form <- ZIO.from(text.fromJson[CreateTodoForm])
          .mapError(msg => new Exception(msg))

        newTodo <- TodoRepository.create(form.title)
      } yield {
        Response.json(newTodo.toJson)
      }
  }
}

object Main extends ZIOAppDefault {

  override val bootstrap =
    Runtime.setConfigProvider(
      TypesafeConfigProvider.fromResourcePath().kebabCase
    ) >>>
    Runtime.removeDefaultLoggers >>>
      zio.logging.consoleLogger() >>>
      Slf4jBridge.initialize

  val prog: ZIO[Server with TodoRepository, Throwable, Unit] = for {
    _ <- ZIO.logInfo("Loading HTTP server...")
    _ <- ZIO.logInfo("Loading AppConfig...")

    port <- Server.install(HttpServer.httpApp.withDefaultErrorResponse)
    _ <- ZIO.logInfo(s"HTTP server started at port $port")
    _ <- ZIO.never
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    prog.provide(
      TodoRepositoryInMemory.layer,
      Server.configured("zio-study.http")
    )
}