import zio._
import zio.json._
import zhttp.http._
import zhttp.service.Server

case class CreateTodoForm(title: String)
object CreateTodoForm {
  implicit val zioJsonCodecForCreateTodoForm: zio.json.JsonCodec[CreateTodoForm] = zio.json.DeriveJsonCodec.gen
}

class TodoRepository {

  private var todoList = Chunk(
    Todo(1, "scala study"),
    Todo(2, "ZIO study")
  )

  def findAll: Task[Chunk[Todo]] = ZIO.succeed(todoList)

  def create(title: String): Task[Todo] = ZIO.succeed {
    val id = todoList.size + 1
    val newTodo = Todo(id, title)
    todoList = todoList :+ newTodo
    newTodo
  }
}

object TodoRepository {
  val layer = ZLayer.succeed(new TodoRepository)
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

  val prog: ZIO[HttpServer, Throwable, Unit] = for {
    server <- ZIO.service[HttpServer]
    _ <- Server.start(8080, server.httpApp)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    prog.provide(
      TodoRepository.layer,
      HttpServer.layer
    )
}