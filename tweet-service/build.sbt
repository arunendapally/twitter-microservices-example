organization := "ch.theza"

name := "tweet-service"

scalaVersion := "2.11.8"

lazy val kafkaVersion = "0.10.0.1"

lazy val specs2Version = "3.8.3-scalaz-7.1"

resolvers ++= Seq(
  "Confluent" at "http://packages.confluent.io/maven/"
)

libraryDependencies ++= Seq(
  "org.twitter4j" % "twitter4j-stream" % "4.0.5",
  "io.confluent" % "kafka-avro-serializer" % "3.0.1" excludeAll(
    ExclusionRule(organization = "com.sun.jdmk"),
    ExclusionRule(organization = "com.sun.jmx"),
    ExclusionRule(organization = "javax.jms"),
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "log4j")
  ),
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "com.typesafe" % "config" % "1.3.1",
  "org.specs2" %% "specs2-core" % specs2Version % "test"
)

scalacOptions ++= Seq("-feature")

scalacOptions in Test ++= Seq("-Yrangepos")

seq( sbtavro.SbtAvro.avroSettings : _*)

(stringType in avroConfig) := "String"
