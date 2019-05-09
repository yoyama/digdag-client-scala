import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "Digdag Client Scala",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"   % "10.1.7",
      "com.typesafe.akka" %% "akka-stream" % "2.5.19", // or whatever the latest version is
      "org.wvlet.airframe" %% "airframe-json" % "19.4.2",
      "org.wvlet.airframe" %% "airframe-codec" % "19.4.2",
      "com.typesafe.play" %% "play-json" % "2.7.2",
      scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
