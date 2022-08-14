import sttp.client3._
import sttp.client3.httpclient.zio._
import sttp.client3.ziojson._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, ServerChannelFactory}
import zio._
import zio.json.{DeriveJsonCodec, JsonCodec}
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
}

object TestAppDriver {

  val layer: ZLayer[Any with EventLoopGroup with ServerChannelFactory with SttpBackend[Task, Any], Throwable, TestAppDriver] =
    ZLayer.scoped {
      for {
        backend <- ZIO.service[SttpBackend[Task, Any]]
        start <- zhttp.service.Server.app(TodoApp.httpApp)
          .withPort(0)
          .make
      } yield new TestAppDriver(start.port, backend)
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
      test("GET /list") {
        for  {
          resp <- ZIO.serviceWithZIO[TestAppDriver](_.getList)
        } yield assertTrue(resp == List(Todo("learn Scala")))
      }
    ).provideSome[EventLoopGroup with ServerChannelFactory](
      HttpClientZioBackend.layer(),
      TestAppDriver.layer,
    ).provideShared(
      EventLoopGroup.auto(1),
      ServerChannelFactory.auto,
    )
}
