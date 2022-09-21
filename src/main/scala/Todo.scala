import zio.json._

final case class Todo(id: Long, title: String)
object Todo {
  implicit val todoJsonCodec: JsonCodec[Todo] = DeriveJsonCodec.gen
}