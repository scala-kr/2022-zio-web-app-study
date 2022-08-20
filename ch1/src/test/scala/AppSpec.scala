import zio.test._

// console를 테스트해보세요
// https://zio.dev/reference/test/
// https://zio.dev/reference/test/services/console/

object AppSpec extends ZIOSpecDefault {
  override def spec = suite("App")(
    test("console test") {
      assertTrue(true)
    },
    test("your test") {
      ???
    }
  )
}
