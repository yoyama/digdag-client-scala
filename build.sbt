lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.1"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / scalaVersion     := scala212
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.github.yoyama"
ThisBuild / organizationName := "yoyama"
ThisBuild / description      := "Scala client library for Digdag"
ThisBuild / licenses         := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage         := Some(url("https://github.com/yoyama/digdag-client-scala"))
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")
ThisBuild / cancelable in Global := true

lazy val root = (project in file("."))
  .aggregate(client)
  .settings(
    name := "digdag-client-scala",
    testFrameworks += new TestFramework("wvlet.airspec.Framework")

  )

lazy val airframeVersion = "19.12.3"
lazy val client = (project in file("digdag-client"))  
  .settings(
    name := "client-lib",
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.wvlet.airframe" %% "airframe-json" % airframeVersion,
      "org.wvlet.airframe" %% "airframe-codec" % airframeVersion,
      "org.wvlet.airframe" %% "airframe-http-finagle" % airframeVersion,
      "org.wvlet.airframe" %% "airspec" % airframeVersion % "test",
      "com.typesafe.play" %% "play-json" % "2.7.4",
      "org.scalactic" %% "scalactic" % "3.0.8",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test
    ),
    testFrameworks += new TestFramework("wvlet.airspec.Framework")
    //fork in run := true
  )


//lazy val shell = (project in file("digdag-shell"))