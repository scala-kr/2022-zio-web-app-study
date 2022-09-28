import io.getquill.Literal
import io.getquill.jdbczio.Quill
import zio.{Chunk, Task, ZLayer}

import javax.sql.DataSource


class TodoRepositoryPostgresql(quill: Quill.Postgres[Literal]) extends TodoRepository {
  import quill._

  override def findAll: Task[Chunk[Todo]] = run(quote {
    query[Todo]
  }).map(Chunk.fromIterable)

  override def create(title: String): Task[Todo] =
    run(quote {
      query[Todo].insert(_.title -> lift(title)).returning(todo => todo)
    })
}

object TodoRepositoryPostgresql {
  val layer: ZLayer[DataSource, Nothing, TodoRepositoryPostgresql] =
    Quill.Postgres.fromNamingStrategy(Literal)
      .project(quill => new TodoRepositoryPostgresql(quill))
}