organization := "ch.theza"

name := "tweet-service"

scalaVersion := "2.11.8"

lazy val kafkaVersion = "0.10.0.0"

libraryDependencies ++= Seq(
  "org.twitter4j" % "twitter4j-stream" % "4.0.5",
  "io.confluent" % "kafka-avro-serializer" % "3.0.0", //TODO is there a newer version of this?
  "org.apache.kafka" % "kafka-clients" % kafkaVersion
)

seq( sbtavro.SbtAvro.avroSettings : _*)

(stringType in avroConfig) := "String"
