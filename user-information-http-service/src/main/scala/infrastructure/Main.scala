package infrastructure

import infrastructure.http4s.Http4sService
import infrastructure.inmemory.InMemoryRawKeyValueStore
import infrastructure.kafka.KeyValueStoreWritingConsumer
import infrastructure.confluentschemaregistry.{RealConfluentAvroPrimitiveSerializer, RealConfluentSpecificRecordDeserializer, SpecificRecordUserReader}
import infrastructure.avro.{User => AvroUser}
import domain.User
import api.{Serializer, Deserializer}

object Main extends infrastructure.http4s.Http4sService {
  override lazy val raw = new InMemoryRawKeyValueStore {}

  val schemaRegistryUrl = "192.168.99.100:8080" //TODO from config
  val topic = "user-information"
  override lazy val userIdSerializer: Serializer[String] = new RealConfluentAvroPrimitiveSerializer[String](schemaRegistryUrl, topic, true)
  override lazy val userDeserializer: Deserializer[User] = new RealConfluentSpecificRecordDeserializer[AvroUser, User](schemaRegistryUrl, false)(SpecificRecordUserReader)

  val kafkaBootstrapServers = "192.168.99.100:9092"
  val applicationInstanceId = "TODO"
  val groupId = s"user-information-http-service-$applicationInstanceId"
  val consumer = new KeyValueStoreWritingConsumer(kafkaBootstrapServers, topic, groupId, raw)
  new Thread(consumer).start()

  //TODO shutdown hook: close consumer, store
}
