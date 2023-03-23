import scala.sys.process._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scala213 = "2.13.6"
lazy val scala212 = "2.12.15"
lazy val supportedScalaVersions = List(scala213, scala212)

lazy val genDigdagShell: TaskKey[Unit] = taskKey[Unit]("Generate digdag-shell executable")

lazy val commonSettings = Seq(
  assembly / test := {},
  assembly / assemblyMergeStrategy := {
    //case PathList(ps@_*) if ps.last contains "io.netty.versions.properties" => MergeStrategy.first
    case x if x contains "io.netty.versions.properties" => MergeStrategy.first
    case "module-info.class" => MergeStrategy.discard
    case x => {
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
    }
  },
)
ThisBuild / scalaVersion     := scala213
ThisBuild / version          := "0.2.0-SNAPSHOT"
ThisBuild / organization     := "io.github.yoyama"
ThisBuild / organizationName := "yoyama"
ThisBuild / description      := "Scala client library for Digdag"
ThisBuild / licenses         := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage         := Some(url("https://github.com/yoyama/digdag-client-scala"))
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")
ThisBuild / cancelable in Global := true
//ThisBuild / coverageEnabled := true
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/yoyama/digdag-client-scala"),
    "scm:git@github.com:yoyama/digdag-client-scala.git"
  )
)
ThisBuild / description := "A Scala client library for Digdag and Digdag Shell"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / isSnapshot := true
//ThisBuild / publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / publishMavenStyle := true

usePgpKeyHex("81F6732F799BC1EDC50A6F2367252D29216B11DC")

lazy val root = (project in file("."))
  .aggregate(client_lib, shell)
  .settings(commonSettings: _*)
  .settings(
    name := "digdag-client-scala",
    testFrameworks += new TestFramework("wvlet.airspec.Framework"),
    crossScalaVersions := Nil
  )

lazy val airframeVersion = "21.3.1"
val excludeJackson = ExclusionRule(organization = "com.fasterxml.jackson")

lazy val client_lib = (project in file("digdag-client"))
  .settings(commonSettings: _*)
  .settings(
    name := "digdag-client-lib-scala",
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "com.typesafe.play" %% "play-json" % "2.9.2" excludeAll(excludeJackson),
      "org.apache.commons" % "commons-compress" % "1.23.0",
      "commons-io" % "commons-io" % "2.11.0",
      "org.wvlet.airframe" %% "airframe-log" % airframeVersion,
      "org.wvlet.airframe" %% "airframe-http-finagle" % airframeVersion  % Test,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "org.scalactic" %% "scalactic" % "3.2.10" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test
    ),
    testFrameworks += new TestFramework("wvlet.airspec.Framework"),
    crossScalaVersions := supportedScalaVersions
    //fork in run := true
  )


lazy val shell = (project in file("digdag-shell"))
  .settings(commonSettings: _*)
  .settings(
    name := "digdag-shell",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scala213,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "org.scalactic" %% "scalactic" % "3.2.10" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test
    ),
    assemblyJarName in assembly := "digdag-shell-assembly.jar",
    genDigdagShell := {
      assembly.value
      println("genDigdagShell called")
      "bin/gen_shell.sh" !
    },
    crossScalaVersions := List(scala213),
  )
  .dependsOn(client_lib)

