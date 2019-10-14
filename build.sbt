ThisBuild / scalaVersion     := "2.12.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")

lazy val root = (project in file("."))
  .aggregate(client)

lazy val airframeVersion = "19.10.1"
lazy val client = (project in file("digdag-client"))  
  .settings(
    name := "client-lib",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"   % "10.1.8",
      "com.typesafe.akka" %% "akka-stream" % "2.5.23", // or whatever the latest version is
      "org.wvlet.airframe" %% "airframe-json" % airframeVersion,
      "org.wvlet.airframe" %% "airframe-codec" % airframeVersion,
      "org.wvlet.airframe" %% "airframe-http-finagle" % airframeVersion,
      "com.typesafe.play" %% "play-json" % "2.7.4",
      "org.scalactic" %% "scalactic" % "3.0.8",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test
    ),
    scalacOptions ++= Seq("-Ypartial-unification")
  )


//lazy val shell = (project in file("digdag-shell"))