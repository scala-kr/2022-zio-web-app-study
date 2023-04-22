import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import zio._
import zio.test._
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.ziojson._
import zio.config.typesafe.TypesafeConfigProvider
import zio.http._
import zio.http.netty.{ChannelFactories, EventLoopGroups, NettyConfig}
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

  val layer: ZLayer[Int, Throwable, TestDriver] =
    ZLayer {
      for {
        start <- ZIO.service[Int]
        backend <- HttpClientZioBackend()
      } yield new TestDriver(start, backend)
    }
}

object MainSpec extends ZIOSpecDefault {

  override val bootstrap = {
    Runtime.setConfigProvider(
      TypesafeConfigProvider.fromResourcePath().kebabCase
    ) >>>
    zio.logging.removeDefaultLoggers >>>
      zio.logging.consoleLogger() >>>
      zio.logging.slf4j.bridge.Slf4jBridge.initialize >>>
      testEnvironment
  }

  val spec: Spec[TestEnvironment with Scope, Throwable] = (suite("Main")(
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
  ).provideSome[DataSource](
    TestDriver.layer,
    TodoRepositoryPostgresql.layer,
    ZLayer.succeed(Server.Config.default.port(0)) ++
      ZLayer.succeed(NettyConfig.default.maxThreads(2)) >>>
      Server.customized,
    ZLayer {
      for {
        port <- Server.install(HttpServer.httpApp.withDefaultErrorResponse)
      } yield port
    }
  ) @@ DbMigrationAspect.migrate()() @@ TestAspect.sequential)
    .provideShared(
      ZPostgreSQLContainer.Settings.default,
      ZPostgreSQLContainer.live
    )
}
