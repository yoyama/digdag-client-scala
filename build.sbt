Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.3"
lazy val supportedScalaVersions = List(scala212, scala213)

lazy val commonSettings = Seq(
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    //case PathList(ps@_*) if ps.last contains "io.netty.versions.properties" => MergeStrategy.first
    case x if x contains "io.netty.versions.properties" => MergeStrategy.first
    case x => {
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
    }
  },
)
ThisBuild / scalaVersion     := scala213
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.github.yoyama"
ThisBuild / organizationName := "yoyama"
ThisBuild / description      := "Scala client library for Digdag"
ThisBuild / licenses         := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage         := Some(url("https://github.com/yoyama/digdag-client-scala"))
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")
ThisBuild / cancelable in Global := true
//ThisBuild / coverageEnabled := true

lazy val root = (project in file("."))
  .aggregate(client, shell)
  .settings(commonSettings: _*)
  .settings(
    name := "digdag-client-scala",
    testFrameworks += new TestFramework("wvlet.airspec.Framework"),
    test in assembly := {}
  )

lazy val airframeVersion = "20.9.2"
lazy val client = (project in file("digdag-client"))
  .settings(commonSettings: _*)
  .settings(
    name := "client-lib",
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "com.typesafe.play" %% "play-json" % "2.7.4",
      "org.apache.commons" % "commons-compress" % "1.20",
      "commons-io" % "commons-io" % "2.8.0",
      "org.wvlet.airframe" %% "airframe-log" % airframeVersion,
      //"org.wvlet.airframe" %% "airframe-json" % airframeVersion % Test,
      //"org.wvlet.airframe" %% "airframe-codec" % airframeVersion % Test,
      "org.wvlet.airframe" %% "airframe-http-finagle" % airframeVersion  % Test,
      "org.scalactic" %% "scalactic" % "3.0.9" % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test
    ),
    testFrameworks += new TestFramework("wvlet.airspec.Framework")
    //fork in run := true
  )


lazy val shell = (project in file("digdag-shell"))
  .settings(commonSettings: _*)
  .settings(
    name := "digdag-shell",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scala213,
      "org.scala-lang" % "scala-library" % scala213,
      "org.scala-lang" % "scala-reflect" % scala213,
      "org.scalactic" %% "scalactic" % "3.0.9" % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test
    ),
    assemblyJarName in assembly := "digdag-shell.jar",
  )
  .dependsOn(client)
