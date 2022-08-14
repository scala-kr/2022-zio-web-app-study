import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Todo(title: String)

object Todo {
  given JsonCodec[Todo] = DeriveJsonCodec.gen[Todo]
}