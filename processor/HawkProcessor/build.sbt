ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "HawkProcessor",
    libraryDependencies ++= Seq(
      // Akka
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
      "com.typesafe.akka" %% "akka-stream" % "2.8.5",
      "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",

      // JSON processing
      "com.typesafe.play" %% "play-json" % "2.10.1",

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",

      // Configuration
      "com.typesafe" % "config" % "1.4.2",

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.8.5" % Test
    )
  )
