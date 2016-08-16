organization := "ch.theza"

name := "user-information-service"

scalaVersion := "2.11.8"

lazy val http4sVersion = "0.14.2"

lazy val specs2Version = "3.8.3-scalaz-7.1"

lazy val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.specs2" %% "specs2-core" % specs2Version % "test",
  "org.specs2" %% "specs2-matcher-extra" % specs2Version % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")
