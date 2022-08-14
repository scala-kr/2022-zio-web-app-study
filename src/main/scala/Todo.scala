import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Todo(title: String)

object Todo {
  implicit val todoJsonCodec: JsonCodec[Todo] = DeriveJsonCodec.gen[Todo]
}