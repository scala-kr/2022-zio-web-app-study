import zio._

// web server를 만들어 보세요
// https://github.com/dream11/zio-http/blob/main/example/src/main/scala/example/HelloWorld.scala

object App extends ZIOAppDefault {
  def run =
    for {
      _ <- Console.print("Please enter your name: ")
      name <- Console.readLine
      _ <- Console.printLine(s"Hello, $name!")
    } yield ()
}
