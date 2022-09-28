import zio._
import zio.json._
import zhttp.http._
import zhttp.service.Server

case class CreateTodoForm(title: String)
object CreateTodoForm {
  implicit val zioJsonCodecForCreateTodoForm: zio.json.JsonCodec[CreateTodoForm] = zio.json.DeriveJsonCodec.gen
}


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

    case req @ Method.POST -> !! / "todo" =>
      for {
        text <- req.body.asCharSeq
        form <- ZIO.from(text.fromJson[CreateTodoForm])
          .mapError(msg => new Exception(msg))

        id = todoList.size + 1
        newTodo = Todo(id, form.title)
      } yield {
        todoList = todoList :+ newTodo
        Response.json(newTodo.toJson)
      }
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Server.start(8080, httpApp)
}