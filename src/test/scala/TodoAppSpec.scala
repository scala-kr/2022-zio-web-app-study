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

  def findById(id: String): Task[Option[Todo]] =
    basicRequest.get(uri"http://localhost:$port/todos/$id")
      .response(asJsonAlways[Option[Todo]])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)

  def addTodo(title: String): Task[Todo] =
    basicRequest.post(uri"http://localhost:$port/todos")
      .response(asJsonAlways[Todo])
      .body(CreateTodo(title))
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)
}

object TestAppDriver {

  val layer: ZLayer[ServerChannelFactory & EventLoopGroup, Throwable, TestAppDriver] =
    ZLayer.scoped {
      (
        for {
          backend <- ZIO.service[SttpBackend[Task, Any]]
          ref <- Ref.make(Chunk.empty[Todo])
          start <- zhttp.service.Server.app(TodoApp.httpApp(ref))
            .withPort(0)
            .make
        } yield new TestAppDriver(start.port, backend)
      ).provideSome(
        HttpClientZioBackend.layer(),
      )
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
          _ <- driver.addTodo("learn ZIO")
          resp <- driver.getList
        } yield assertTrue(resp == List(Todo(1, "learn ZIO")))
      }
    ).provideSome[EventLoopGroup & ServerChannelFactory](
      TestAppDriver.layer,
    ).provideShared(
      EventLoopGroup.auto(1),
      ServerChannelFactory.auto,
    )
}
