val http4sVersion = "0.23.23"

lazy val root = project
  .in(file("."))
  .settings(
    name := "hell-o-world",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := "3.3.1",

    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.17",
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
    )
  )
