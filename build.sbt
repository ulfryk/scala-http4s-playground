lazy val http4sVersion = "0.23.29"
lazy val scalaStaticVersion = "3.2.19"
lazy val circeVersion = "0.14.10"
lazy val skunkVersion = "0.6.4"
lazy val doobieVersion = "1.0.0-RC6"

ThisBuild / scalacOptions ++= Seq("-source", "future")
ThisBuild / scalacOptions ++= Seq("-feature")

Compile / run / fork := true

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
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.16",
      "org.tpolecat" %% "skunk-core" % skunkVersion,
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.8",
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "org.gnieh" %% "fs2-data-csv" % "1.11.1",
      "org.gnieh" %% "fs2-data-csv-generic" % "1.11.1",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-specs2" % doobieVersion
    )
  )
