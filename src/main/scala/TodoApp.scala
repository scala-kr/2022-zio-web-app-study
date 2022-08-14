import zhttp.http._
import zio._
import zio.json._


object port {

  object in {

    trait Hello {
      def hello: UIO[String]
    }


    trait ListAllTodos {
      def listAllTodos: Task[List[Todo]]
    }

    trait SaveTodo {
      def saveTodo(todo: Todo): Task[Unit]

    }
  }
}

object adapter {

  object in {

    import port.in._

    class TodoAdapterInMemory(todos: Ref[Chunk[Todo]]) extends ListAllTodos with SaveTodo {

    override def listAllTodos: Task[List[Todo]] =
      todos.get.map(_.toList)

    override def saveTodo(todo: Todo): Task[Unit] =
      todos.update(_ :+ todo).unit
  }

    object TodoAdapterInMemory {

      val layer: ZLayer[Any, Nothing, TodoAdapterInMemory] = ZLayer {
        for {
          ref <- Ref.make(Chunk.empty[Todo])
        } yield new TodoAdapterInMemory(ref)
      }
    }


    class HelloAdapter extends port.in.Hello {

      override def hello: UIO[String] =
        ZIO.succeed("hello")
    }

    object HelloAdapter {
      val layer: ULayer[HelloAdapter] =
        ZLayer.succeed(new HelloAdapter())
    }
  }
}


object TodoApp {
  import port.in._

  def httpApp: Http[Hello & ListAllTodos & SaveTodo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "hello" =>
        ZIO.serviceWithZIO[Hello](_.hello).map(Response.text)

      case Method.GET -> !! / "todos" =>
        for {
          todos <- ZIO.serviceWithZIO[ListAllTodos](_.listAllTodos)
        } yield Response.json(todos.toJson)

      case req @ Method.POST -> !! / "todos" =>
        for {
          body <- req.bodyAsCharSequence
          todo <- ZIO.from(body.fromJson[Todo]).mapError(err => new Exception(err))
          _ <- ZIO.serviceWithZIO[SaveTodo](_.saveTodo(todo))
        } yield Response.json("""{ "succeess": true }""")
    }
}