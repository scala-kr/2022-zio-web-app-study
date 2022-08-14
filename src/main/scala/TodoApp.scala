import zhttp.http._
import zio._
import zio.json._


case class TodoApp(todos: Ref[Chunk[Todo]]) {

  def httpApp: Http[Any, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "hello" =>
        ZIO.succeed(Response.text("hello"))

      case Method.GET -> !! / "todos" =>
        for {
          todos <- todos.get
        } yield Response.json(todos.toJson)

      case req @ Method.POST -> !! / "todos" =>
        for {
          body <- req.bodyAsCharSequence
          todo <- ZIO.from(body.fromJson[Todo]).mapError(err => new Exception(err))
          _ <- todos.update(_ :+ todo)
        } yield Response.json("""{ "succeess": true }""")
    }
}

object TodoApp {
  val layer: ZLayer[Any, Nothing, TodoApp] = ZLayer {
    for {
      ref <- Ref.make(Chunk.empty[Todo])
    } yield TodoApp(ref)
  }
}