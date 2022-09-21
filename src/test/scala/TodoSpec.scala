import zio.test._

object TodoSpec extends ZIOSpecDefault {
  override val spec = suite("Todo")(
    test("Serialized to JSON") {
      import zio.json._

      val todo = Todo(42, "nice")
      val expected = """{"id":42,"title":"nice"}"""

      assertTrue(todo.toJson == expected)
    }
  )
}
