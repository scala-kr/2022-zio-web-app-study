import zio._

import java.io.IOException

// console https://zio.dev/guides/quickstarts/hello-world

object App extends ZIOAppDefault {
  val prog: ZIO[Any, IOException, Unit] = for {
    _ <- Console.print("Please enter your name: ")
    name <- Console.readLine
    _ <- Console.printLine(s"Hello, $name!")
  } yield ()

  def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    prog
  }
}
