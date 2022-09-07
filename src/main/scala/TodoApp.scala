import zhttp.http._
import zhttp.service.Server
import zio._
import zio.json._

case class CreateTodo(title: String)
object CreateTodo {
  implicit val jsonCodecForCreateTodo: JsonCodec[CreateTodo] = DeriveJsonCodec.gen
}

object TodoApp {

  def httpApp(todos: Ref[Chunk[Todo]]): Http[Any, Throwable, Request, Response] =
    Http.collectZIO[Request] {

      case Method.GET -> !! / "todos" =>
        for {
          todos <- todos.get.map(_.toList)
        } yield Response.json(todos.toJson)

      case Method.GET -> !! / "todos" / idStr =>
        ZIO.foreach(idStr.toLongOption) { id =>
          todos.get.map(_.find(_.id == id))
        }.map {
          case Some(todo) => Response.json(todo.toJson)
          case None => Response.status(Status.NotFound)
        }

      case req @ Method.POST -> !! / "todos" =>
        for {
          body <- req.bodyAsCharSequence
          form <- ZIO.from(body.fromJson[CreateTodo]).mapError(err => new Exception(err))
          newTodo <- todos.modify { all =>
            val todo = Todo(all.length + 1, form.title)
            (todo, all :+ todo)
          }
        } yield Response.json(newTodo.toJson).setStatus(Status.Created)
    }
}

object Main extends ZIOAppDefault {
  def run =  for {
    ref <- Ref.make(Chunk.empty[Todo])
    _ <- Server.start(8080, TodoApp.httpApp(ref))
  } yield ()
}