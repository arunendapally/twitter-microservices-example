package infrastructure

import java.util.Properties
import org.apache.kafka.streams.{StreamsConfig, KafkaStreams}
import org.apache.kafka.streams.kstream.{KStreamBuilder, KStream}
import org.apache.kafka.streams.processor.TopologyBuilder
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import com.typesafe.config.ConfigFactory
import infrastructure.kafka.{KafkaAvroSerde, SpecificKafkaAvroSerde, RandomFollowProcessor}
import org.slf4j.LoggerFactory
import domain.User

object Main extends App {
  val config = ConfigFactory.load()
  val logger = LoggerFactory.getLogger(getClass)

  val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getString("follow-service.application-id"))
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("follow-service.kafka-bootstrap-servers"))
  props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, config.getString("follow-service.zookeeper-connect"))
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getString("follow-service.schema-registry-url"))
  props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, classOf[KafkaAvroSerde])
  props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, classOf[SpecificKafkaAvroSerde])
  val streamsConfig = new StreamsConfig(props)

  val usersTopic = config.getString("follow-service.users-topic")
  val followsTopic = config.getString("follow-service.follows-topic")

  val builder = new TopologyBuilder

  builder
    .addSource(usersTopic, usersTopic)
    .addProcessor("RandomFollowProcessor", new RandomFollowProcessor, usersTopic)
    .addSink(followsTopic, followsTopic, "RandomFollowProcessor")

  val streams = new KafkaStreams(builder, streamsConfig)
  streams.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
    override def uncaughtException(thread: Thread, e: Throwable) {
        logger.error(s"Uncaught exception on thread $thread", e)
    }
  })
  streams.start()

  scala.sys.addShutdownHook {
    streams.close()
  }
}
