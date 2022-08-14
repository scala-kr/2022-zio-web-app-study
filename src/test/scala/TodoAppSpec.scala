import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.DataSource
import io.github.scottweaver.models.JdbcInfo
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import sttp.client3._
import sttp.client3.httpclient.zio._
import sttp.client3.ziojson._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, ServerChannelFactory}
import zio._
import zio.test._

import javax.sql.DataSource


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

  val quillDataSource: ZLayer[JdbcInfo, Throwable, DataSource] =
    ZLayer.service[JdbcInfo].flatMap { env =>
      val jdbcInfo = env.get
      val ds = new PGSimpleDataSource
      ds.setURL(jdbcInfo.jdbcUrl)
      ds.setUser(jdbcInfo.username)
      ds.setPassword(jdbcInfo.password)
      Quill.DataSource.fromDataSource(ds)
    }

  val layer: ZLayer[JdbcInfo & ServerChannelFactory & EventLoopGroup, Throwable, TestAppDriver] =
    ZLayer.scoped {
      (
        for {
          backend <- ZIO.service[SttpBackend[Task, Any]]
          start <- zhttp.service.Server.app(TodoApp.httpApp)
            .withPort(0)
            .make
        } yield new TestAppDriver(start.port, backend)
      ).provideSome(
        HttpClientZioBackend.layer(),
        adapter.in.HelloAdapter.layer,
        TodoRepositoryPostgres.layer,
        quillDataSource,
      )
    }

}

object TodoAppSpec extends ZIOSpecDefault {

  override def spec =
    (suite("TodoApp")(
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
    ) @@ DbMigrationAspect.migrate()()).provideSome[EventLoopGroup & ServerChannelFactory](
      TestAppDriver.layer,
      ZPostgreSQLContainer.live,
      ZPostgreSQLContainer.Settings.default,
    ).provideShared(
      EventLoopGroup.auto(1),
      ServerChannelFactory.auto,
    )
}
