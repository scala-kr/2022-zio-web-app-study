import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import zio._
import zio.test._
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.ziojson._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}
import zio.test.Assertion.equalTo



class TestDriver(port: Int, backend: SttpBackend[Task, ZioStreams with capabilities.WebSockets]) {
  def hello: ZIO[Any, Throwable, String] =
    for {
      res <-
        basicRequest.get(uri"http://localhost:${port}/hello")
          .response(asStringAlways)
          .send(backend)
    } yield res.body


  def list: ZIO[Any, Throwable, List[Todo]] =
    for {
      resp <- basicRequest.get(uri"http://localhost:${port}/todo/list")
        .response(asJsonAlways[List[Todo]])
        .send(backend)

      body <- ZIO.from(resp.body)

    } yield body

}

object TestDriver {
  def hello: RIO[TestDriver, String] = ZIO.serviceWithZIO[TestDriver](_.hello)
  def list: RIO[TestDriver, List[Todo]] = ZIO.serviceWithZIO[TestDriver](_.list)

  val layer: ZLayer[Server.Start, Throwable, TestDriver] =
    ZLayer {
      for {
        start <- ZIO.service[Server.Start]
        backend <- HttpClientZioBackend()
      } yield new TestDriver(start.port, backend)
    }
}

object MainSpec extends ZIOSpec[EventLoopGroup with ServerChannelFactory] {

  override val bootstrap: ZLayer[Scope, Any, EventLoopGroup with ServerChannelFactory] =
    EventLoopGroup.auto(1) ++ ServerChannelFactory.auto

  val spec = suite("Main")(
    test("GET /hello returns 'hello'") {
      assertZIO(TestDriver.hello)(equalTo("hello"))
    },
    test("GET /todo/list returns a hardcoded list of todos") {
      val expected = List(
        Todo(1, "scala study"),
        Todo(2, "ZIO study"),
      )
      assertZIO(TestDriver.list)(equalTo(expected))

    }
  ).provideSome(
    TestDriver.layer,
    ZLayer {
      Server.app(Main.httpApp).withPort(0).make
    }
  )
}
