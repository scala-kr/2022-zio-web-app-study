ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val zioVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "zio-web-study",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "com.softwaremill.sttp.client3" %% "zio" % "3.7.4",
      "dev.zio" %% "zio-test" % zioVersion % Test,
    )
  )
