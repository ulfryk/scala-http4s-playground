val http4sVersion = "0.23.27"
val scalaStaticVersion = "3.2.18"

lazy val root = project
  .in(file("."))
  .settings(
    name := "hell-o-world",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := "3.3.1",

    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % scalaStaticVersion,
      "org.scalatest" %% "scalatest" % scalaStaticVersion % Test,
      "com.comcast" %% "ip4s-core" % "3.6.0",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.13",
    )
  )
