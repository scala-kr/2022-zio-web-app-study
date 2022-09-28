import zio.{Chunk, Task}

trait TodoRepository {

  def findAll: Task[Chunk[Todo]]

  def create(title: String): Task[Todo]
}
