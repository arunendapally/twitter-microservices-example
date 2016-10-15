package infrastructure

import infrastructure.http4s.Http4sService
import infrastructure.inmemory.InMemoryRawKeyValueStore
import infrastructure.kafka.KeyValueStoreWritingConsumer
import infrastructure.confluentschemaregistry.{RealConfluentAvroPrimitiveSerializer, RealConfluentSpecificRecordDeserializer, SpecificRecordUserReader}
import domain.{UserInformation => AvroUser}
import domain.User
import api.{Serializer, Deserializer}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main extends Http4sService {
  val config = ConfigFactory.load()
  val logger = LoggerFactory.getLogger(getClass)

  override lazy val raw = new InMemoryRawKeyValueStore {}

  val schemaRegistryUrl = config.getString("user-information-http-service.schema-registry-url")
  val topic = config.getString("user-information-http-service.user-information-topic")
  override lazy val userIdSerializer: Serializer[String] = new RealConfluentAvroPrimitiveSerializer[String](schemaRegistryUrl, topic, true)
  override lazy val userDeserializer: Deserializer[User] = new RealConfluentSpecificRecordDeserializer[AvroUser, User](schemaRegistryUrl, false)(SpecificRecordUserReader)

  val kafkaBootstrapServers = config.getString("user-information-http-service.kafka-bootstrap-servers")
  val applicationInstanceId = "TODO" //needs to be a value unique to this app instance, stable across runs, multiple instances on same machine must be unique
  val groupId = s"user-information-http-service-$applicationInstanceId"
  val consumer = new KeyValueStoreWritingConsumer(kafkaBootstrapServers, topic, groupId, raw)
  new Thread(consumer).start()

  scala.sys.addShutdownHook {
    consumer.close()
    logger.info("Shutting down")
  }
}
