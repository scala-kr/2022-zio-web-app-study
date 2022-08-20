import zio.test._

// 서버를 테스트해보세요
// https://zio.dev/reference/test/
// https://sttp.softwaremill.com/en/latest/quickstart.html
// https://sttp.softwaremill.com/en/latest/backends/zio.html

object AppSpec extends ZIOSpecDefault {
  override def spec = suite("App")(
    test("request test") {
      assertTrue(true)
    },
    test("your test") {
      ???
    }
  )
}
