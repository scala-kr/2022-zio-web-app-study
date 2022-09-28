import io.github.scottweaver.models.JdbcInfo
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
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

import javax.sql.DataSource



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

  def add(title: String) =
    for {
      // { "title": "A new item" }
      resp <- basicRequest.post(uri"http://localhost:${port}/todo")
        .body(s"""{"title":"$title"}""")
        .header("Content-Type", "application/json")
        .response(asJsonAlways[Todo])
        .send(backend)
      body <- ZIO.from(resp.body)
    } yield body
}

object TestDriver {
  def hello: RIO[TestDriver, String] = ZIO.serviceWithZIO[TestDriver](_.hello)
  def list: RIO[TestDriver, List[Todo]] = ZIO.serviceWithZIO[TestDriver](_.list)
  def add(title: String): RIO[TestDriver, Todo] = ZIO.serviceWithZIO[TestDriver](_.add(title))

  val layer: ZLayer[Server.Start, Throwable, TestDriver] =
    ZLayer {
      for {
        start <- ZIO.service[Server.Start]
        backend <- HttpClientZioBackend()
      } yield new TestDriver(start.port, backend)
    }
}

object MainSpec extends ZIOSpec[EventLoopGroup with ServerChannelFactory] {

  override val bootstrap: ZLayer[Scope, Any, Environment] =
    EventLoopGroup.auto(1) ++ ServerChannelFactory.auto

  val spec = (suite("Main")(
    test("GET /hello returns 'hello'") {
      assertZIO(TestDriver.hello)(equalTo("hello"))
    },
    test("GET /todo/list returns a hardcoded list of todos") {
      val expected = List(
        Todo(1, "scala study"),
        Todo(2, "ZIO study"),
      )
      assertZIO(TestDriver.list)(equalTo(expected))

    },
    test("POST /todo adds a todo item to the list") {
      val title = "A new item"
      // old list doesn't have the todo item
      // add a todo with title
      // new items exists in the list
      for {
        oldList <- TestDriver.list
        _ <- assertTrue(!oldList.exists(_.title == title))

        newItem <- TestDriver.add(title)
        newList <- TestDriver.list

      } yield assertTrue(newList.contains(newItem))
    },
  ).provideSome[Scope with Environment with DataSource](
    TestDriver.layer,
    TodoRepositoryPostgresql.layer,
    HttpServer.layer,
    ZLayer {
      for {
        httpServer <- ZIO.service[HttpServer]
        start <- Server.app(httpServer.httpApp).withPort(0).make
      } yield start
    }
  ) @@ DbMigrationAspect.migrate()() @@ TestAspect.sequential)
    .provideSomeShared(
      ZPostgreSQLContainer.Settings.default,
      ZPostgreSQLContainer.live
    )
}
