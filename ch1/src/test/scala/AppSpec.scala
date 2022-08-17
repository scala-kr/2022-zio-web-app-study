import sttp.client3._
import zio.test._

// 서버를 테스트해보세요

object AppSpec extends ZIOSpecDefault {
  override def spec = suite("App")(
    test("request test") {
      val request = basicRequest.get(uri"https://google.com")
      val backend = HttpClientSyncBackend()
      val response = request.send(backend)

      assertTrue(response.code.code == 200)
    },
    test("your test") {
      assertTrue(true)
    }
  )
}
