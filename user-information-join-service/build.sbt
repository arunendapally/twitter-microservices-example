organization := "ch.theza"

name := "user-information-join-service"

scalaVersion := "2.11.8"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

lazy val kafkaVersion = "0.10.0.1-cp1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.avro" % "avro" % "1.7.7",
  "io.confluent" % "kafka-avro-serializer" % "3.0.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe" % "config" % "1.3.1"
)

seq( sbtavro.SbtAvro.avroSettings : _*)

(stringType in avroConfig) := "String"

scalacOptions ++= Seq("-feature")
