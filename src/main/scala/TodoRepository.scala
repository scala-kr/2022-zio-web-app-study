import zio.macros.accessible
import zio.{Chunk, RIO, Task, ZIO}

@accessible
trait TodoRepository {
  def findAll: Task[Chunk[Todo]]

  def create(title: String): Task[Todo]
}