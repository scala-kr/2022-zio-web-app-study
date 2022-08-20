ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val zioVersion = "2.0.0"
val sttpVersion = "3.7.4"
val sttpClientVersion = "3.7.4"


lazy val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    "io.d11" %% "zhttp" % "2.0.0-RC10",
    "com.softwaremill.sttp.client3" %% "core" % sttpClientVersion,
    "com.softwaremill.sttp.client3" %% "zio" % sttpClientVersion,
    "com.softwaremill.sttp.client3" %% "zio" % sttpVersion,
    "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
    "dev.zio" %% "zio-json" % "0.3.0-RC10",
  ),
)

lazy val root = (project in file("."))
  .settings(
    name := "2022-zio-web-study"
  )

lazy val ch1 = project
  .settings(sharedSettings)

lazy val ch2 = project
  .settings(sharedSettings)
