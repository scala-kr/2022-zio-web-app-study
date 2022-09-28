scalaVersion := "2.13.8"

val V = new {
  val zio = "2.0.2"
  val sttp = "3.8.0"
  val zioTestContainer = "0.8.0"
}


libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % V.zio,
  "com.softwaremill.sttp.client3" %% "zio" % V.sttp,
  "com.softwaremill.sttp.client3" %% "zio-json" % V.sttp,
  "io.d11" %% "zhttp" % "2.0.0-RC11",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.getquill" %% "quill-jdbc-zio" % "4.4.1",
  "org.postgresql" % "postgresql" % "42.3.1",
  "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % V.zioTestContainer % Test,
  "io.github.scottweaver" %% "zio-2-0-db-migration-aspect" % V.zioTestContainer % Test,
  "dev.zio" %% "zio-test" % V.zio % Test,
  "dev.zio" %% "zio-test-sbt" % V.zio % Test,
)