import zio._
import zio.test._
import sttp.client3._
import sttp.client3.httpclient.zio._


object TodoAppSpec extends ZIOSpecDefault {

  override def spec =
    suite("TodoApp")(
      test("/hello works") {
        for {
          resp <- basicRequest
            .get(uri"http://localhost:8080/hello")
            .sendZIO
        } yield assertTrue(resp.body == Right("hello"))
      }
    ).provide(
      HttpClientZioBackend.layer()
    )


  implicit class SendZIOSyntax[T, -R >: sttp.capabilities.Effect[Task]](val req: Request[T, R]) extends AnyVal {
    def sendZIO: ZIO[SttpBackend[Task, Any], Throwable, Response[T]] = {
      ZIO.serviceWithZIO[SttpBackend[Task, Any]](_.send(req))
    }
  }

}
