organization := "ch.theza"

name := "user-information-http-service"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Confluent" at "http://packages.confluent.io/maven/"
)

lazy val http4sVersion = "0.14.2"

lazy val specs2Version = "3.8.3-scalaz-7.1"

lazy val circeVersion = "0.4.1"

lazy val kafkaVersion = "0.10.0.0"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "io.confluent" % "kafka-avro-serializer" % "3.0.0", //TODO is there a newer version of this?
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.rocksdb" % "rocksdbjni" % "4.9.0",
  "commons-io" % "commons-io" % "2.5",
  "org.specs2" %% "specs2-core" % specs2Version % "test",
  "org.specs2" %% "specs2-matcher-extra" % specs2Version % "test",
  "org.apache.kafka" %% "kafka" % kafkaVersion % "test",
  "org.apache.kafka" %% "kafka" % kafkaVersion % "test" classifier "test",
  "org.apache.kafka" % "kafka-clients" % kafkaVersion % "test" classifier "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

seq( sbtavro.SbtAvro.avroSettings : _*)

(stringType in avroConfig) := "String"
