import zio._
import zio.test._
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}


object MainSpec extends ZIOSpec[EventLoopGroup with ServerChannelFactory] {

  override val bootstrap: ZLayer[Scope, Any, EventLoopGroup with ServerChannelFactory] =
    EventLoopGroup.auto(1) ++ ServerChannelFactory.auto

  val spec = suite("Main")(
    test("GET /hello returns 'hello'") {
      // GET localhost:8080/hello
      // 응답이 "hello"인지 확인한다.
      HttpClientZioBackend().flatMap { backend =>
        for {
          start <- ZIO.service[Server.Start]
          _ <- Console.printLine(s"Server started on port ${start.port}")
          res <-
            basicRequest.get(uri"http://localhost:${start.port}/hello")
              .response(asString)
              .send(backend)
        } yield assertTrue(res.body == Right("hello"))
      }
    },
  ).provideSome(
    ZLayer {
      Server.app(Main.httpApp).withPort(0).make
    }
  )
}
