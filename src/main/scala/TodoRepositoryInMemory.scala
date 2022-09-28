import zio.{Chunk, Task, ZIO, ZLayer}

class TodoRepositoryInMemory extends TodoRepository {

  private var todoList = Chunk(
    Todo(1, "scala study"),
    Todo(2, "ZIO study")
  )

  def findAll: Task[Chunk[Todo]] = ZIO.succeed(todoList)

  def create(title: String): Task[Todo] = ZIO.succeed {
    val id = todoList.size + 1
    val newTodo = Todo(id, title)
    todoList = todoList :+ newTodo
    newTodo
  }
}

object TodoRepositoryInMemory {
  val layer = ZLayer.succeed(new TodoRepositoryInMemory)
}