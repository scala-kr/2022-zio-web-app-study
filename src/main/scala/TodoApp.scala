import zhttp.http._
import zio.json._


object TodoApp {

  def httpApp: Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      case Method.GET -> !! / "hello" =>
        Response.text("hello")

      case Method.GET -> !! / "todos" =>
        val todos = List(Todo("learn Scala"))
        Response.json(todos.toJson)
    }
}
