name := "prelude-utils"

version := "0.0.0"

lazy val root = project.in(file("."))
  .settings(libraryDependencies ++= commonDeps)
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .configs(IntegrationTest.extend(Test))
  .settings(Defaults.itSettings)

lazy val sttp = "1.1.8"
lazy val circe = "0.9.1"

lazy val commonDeps = Seq(
  "com.softwaremill.sttp" %% "core" % sttp,
  "com.softwaremill.sttp" %% "circe" % sttp,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "io.circe" %% "circe-core" % circe,
  "io.circe" %% "circe-generic" % circe,
  "io.circe" %% "circe-parser" % circe,
  "io.estatico" %% "newtype" % "0.3.0",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.typelevel" %% "cats-effect" % "0.9",
  "org.typelevel" %% "kittens" % "1.0.0-RC2",
  "org.typelevel" %% "mouse" % "0.16",

  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test, it",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test, it",
  "org.scodec" %% "scodec-core" % "1.10.3" % "test, it"
)

scalacOptions in Global ++= Seq(
  "-Ypartial-unification",
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions"
)
