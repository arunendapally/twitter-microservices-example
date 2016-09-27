package infrastructure

import infrastructure.http4s.Http4sService
import infrastructure.inmemory.InMemoryRawKeyValueStore
import infrastructure.kafka.KeyValueStoreWritingConsumer
import domain.User
import api.{Serializer, Deserializer}

object Main extends infrastructure.http4s.Http4sService {
  override lazy val raw = new InMemoryRawKeyValueStore {}
  override lazy val userIdSerializer: Serializer[String] = ???
  override lazy val userDeserializer: Deserializer[User] = ???

  val applicationInstanceId = "TODO"

  val consumer = new KeyValueStoreWritingConsumer(
    "192.168.99.100:9092", 
    "user-information", 
    s"user-information-http-service-$applicationInstanceId", 
    raw)
  new Thread(consumer).start()

  //TODO shutdown hook: close consumer, store
}
