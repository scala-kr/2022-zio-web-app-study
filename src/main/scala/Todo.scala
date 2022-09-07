import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Todo(id: Long, title: String)

object Todo {
  implicit val todoJsonCodec: JsonCodec[Todo] = DeriveJsonCodec.gen[Todo]
}