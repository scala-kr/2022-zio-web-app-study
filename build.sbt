scalaVersion := "2.13.10"

val V = new {
  val zio = "2.0.13"
  val sttp = "3.8.15"
  val zioTestContainer = "0.10.0"
  val zioConfig = "4.0.0-RC14"
  val zioLogging = "2.1.12"
}

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % V.zio,
  "dev.zio" %% "zio-config" % V.zioConfig,
  "dev.zio" %% "zio-config-magnolia" % V.zioConfig,
  "dev.zio" %% "zio-config-typesafe" % V.zioConfig,
  "dev.zio" %% "zio-logging" % V.zioLogging,
  "dev.zio" %% "zio-logging-slf4j-bridge" % V.zioLogging,
  "dev.zio" %% "zio-http" % "3.0.0-RC1",
  "com.softwaremill.sttp.client3" %% "zio" % V.sttp,
  "com.softwaremill.sttp.client3" %% "zio-json" % V.sttp,
  "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
  "org.postgresql" % "postgresql" % "42.6.0",
  "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % V.zioTestContainer % Test,
  "io.github.scottweaver" %% "zio-2-0-db-migration-aspect" % V.zioTestContainer % Test,
  "dev.zio" %% "zio-test" % V.zio % Test,
  "dev.zio" %% "zio-test-sbt" % V.zio % Test,
)

Test / fork := true