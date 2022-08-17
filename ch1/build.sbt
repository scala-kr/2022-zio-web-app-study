ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

val zioVersion = "2.0.0"
val sttpClientVersion = "3.7.4"

lazy val root = (project in file("."))
  .settings(
    name := "ch1",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "com.softwaremill.sttp.client3" %% "core" % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "zio" % sttpClientVersion
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
