package infrastructure

import java.util.Properties
import org.apache.kafka.streams.{StreamsConfig, KafkaStreams}
import org.apache.kafka.streams.kstream.KStreamBuilder
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import com.typesafe.config.ConfigFactory
import infrastructure.kafka.{UserInformationJoinService, KafkaAvroSerde, SpecificKafkaAvroSerde}
import org.slf4j.LoggerFactory

object Main extends App {
  val config = ConfigFactory.load()
  val logger = LoggerFactory.getLogger(getClass)

  val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getString("user-information-join-service.application-id"))
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("user-information-join-service.kafka-bootstrap-servers"))
  props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, config.getString("user-information-join-service.zookeeper-connect"))
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getString("user-information-join-service.schema-registry-url"))
  props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, classOf[KafkaAvroSerde])
  props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, classOf[SpecificKafkaAvroSerde])
  val streamsConfig = new StreamsConfig(props)

  val builder = new KStreamBuilder
  UserInformationJoinService.build(
    config.getString("user-information-join-service.users-topic"), 
    config.getString("user-information-join-service.tweets-topic"), 
    config.getString("user-information-join-service.user-information-topic"),
    builder)

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
