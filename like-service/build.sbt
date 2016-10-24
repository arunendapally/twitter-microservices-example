organization := "ch.theza"

name := "like-service"

scalaVersion := "2.11.8"

resolvers += "Confluent" at "http://packages.confluent.io/maven/"

lazy val kafkaVersion = "0.10.0.1-cp1"

lazy val specs2Version = "3.8.3-scalaz-7.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-streams" % kafkaVersion excludeAll(
    ExclusionRule(organization = "com.sun.jdmk"),
    ExclusionRule(organization = "com.sun.jmx"),
    ExclusionRule(organization = "javax.jms"),
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "log4j")
  ),
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "io.confluent" % "kafka-avro-serializer" % "3.0.1" excludeAll(
    ExclusionRule(organization = "com.sun.jdmk"),
    ExclusionRule(organization = "com.sun.jmx"),
    ExclusionRule(organization = "javax.jms"),
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "log4j")
  ),
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "com.typesafe" % "config" % "1.3.1",
  "org.specs2" %% "specs2-core" % specs2Version % "test",
  "org.apache.kafka" % "kafka-streams" % kafkaVersion % "test" classifier "test"
)

scalacOptions ++= Seq("-feature")

scalacOptions in Test ++= Seq("-Yrangepos")

seq( sbtavro.SbtAvro.avroSettings : _*)

(stringType in avroConfig) := "String"
