scalaVersion := "2.13.8"

val V = new {
  val zio = "2.0.2"
  val sttp = "3.8.0"
}

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % V.zio,
  "com.softwaremill.sttp.client3" %% "zio" % V.sttp,
  "com.softwaremill.sttp.client3" %% "zio-json" % V.sttp,
  "io.d11" %% "zhttp" % "2.0.0-RC11",
  "dev.zio" %% "zio-test" % V.zio % Test,
  "dev.zio" %% "zio-test-sbt" % V.zio % Test,
)