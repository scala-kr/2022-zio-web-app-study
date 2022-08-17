import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.test._

// 서버를 테스트해보세요

object AppSpec extends ZIOSpecDefault {
  override def spec = suite("App")(
    test("request test") {
      val getGoogle = basicRequest.get(uri"https://google.com")
      for {
        backend <- HttpClientZioBackend()
        resp <- getGoogle.send(backend)
      } yield assertTrue(resp.code.code == 200)
    },
    test("your test") {
      assertTrue(true)
    }
  )
}
