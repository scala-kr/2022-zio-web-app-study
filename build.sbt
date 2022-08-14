ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioVersion = "2.0.0"
val sttpVersion = "3.7.4"

lazy val root = (project in file("."))
  .settings(
    name := "zio-web-study",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "com.softwaremill.sttp.client3" %% "zio-json" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "zio" % sttpVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    ),
  )
