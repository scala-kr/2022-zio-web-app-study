import io.getquill.{PostgresDialect, SnakeCase}
import port.in.{ListAllTodos, SaveTodo}
import zio.{Task, ZLayer}
import io.getquill.jdbczio.Quill

import javax.sql.DataSource

class TodoRepositoryPostgres(quill: Quill[PostgresDialect, SnakeCase]) extends ListAllTodos with SaveTodo {
  import quill._

  override def listAllTodos: Task[List[Todo]] = run(query[Todo])

  override def saveTodo(todo: Todo): Task[Unit] = run(query[Todo].insertValue(lift(todo))).unit
}

object TodoRepositoryPostgres {

  val layer: ZLayer[DataSource, Nothing, TodoRepositoryPostgres] =
    Quill.Postgres.fromNamingStrategy(SnakeCase).project(new TodoRepositoryPostgres(_))
}