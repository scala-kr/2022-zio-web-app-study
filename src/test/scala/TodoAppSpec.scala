import sttp.client3._
import sttp.client3.httpclient.zio._
import sttp.client3.ziojson._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, ServerChannelFactory}
import zio._
import zio.test._


class TestAppDriver(port: Int, backend: SttpBackend[Task, Any]) {

  def getHello: ZIO[Any, Either[String, Throwable], String] =
    basicRequest.get(uri"http://localhost:$port/hello")
      .send(backend)
      .map(_.body)
      .right

  def getList: Task[List[Todo]] =
    basicRequest.get(uri"http://localhost:$port/todos")
      .response(asJsonAlways[List[Todo]])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)

  def addTodo(todo: Todo) =
    basicRequest.post(uri"http://localhost:$port/todos")
      .body(todo)
      .send(backend)
}

object TestAppDriver {

  val layer: ZLayer[ServerChannelFactory with EventLoopGroup, Throwable, TestAppDriver] = {
    HttpClientZioBackend.layer() ++
      TodoApp.layer >>>
      ZLayer.scoped {
        for {
          todoApp <- ZIO.service[TodoApp]
          backend <- ZIO.service[SttpBackend[Task, Any]]
          start <- zhttp.service.Server.app(todoApp.httpApp)
            .withPort(0)
            .make
        } yield new TestAppDriver(start.port, backend)
      }
    }

}

object TodoAppSpec extends ZIOSpecDefault {

  override def spec =
    suite("TodoApp")(
      test("/hello works") {
        for {
          resp <- ZIO.serviceWithZIO[TestAppDriver](_.getHello)
        } yield assertTrue(resp == "hello")
      },
      test("POST /todos works") {
        for {
          driver <- ZIO.service[TestAppDriver]
          _ <- driver.addTodo(Todo("learn ZIO"))
          resp <- driver.getList
        } yield assertTrue(resp == List(Todo("learn ZIO")))
      }
    ).provideSome[EventLoopGroup with ServerChannelFactory](
      TestAppDriver.layer,
    ).provideShared(
      EventLoopGroup.auto(1),
      ServerChannelFactory.auto,
    )
}
