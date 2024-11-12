lazy val http4sVersion = "0.23.27"
lazy val scalaStaticVersion = "3.2.18"
lazy val circleVersion = "0.14.8"
lazy val doobieVersion = "1.0.0-RC5"

//ThisBuild / scalacOptions ++= Seq("-source", "future")

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
      "io.circe" %% "circe-generic" % circleVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "org.tpolecat" %% "skunk-core" % "0.6.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-specs2" % doobieVersion
    )
  )
