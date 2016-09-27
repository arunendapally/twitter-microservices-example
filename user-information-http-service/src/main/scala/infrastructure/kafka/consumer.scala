package infrastructure.kafka

import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.JavaConversions._
import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerRecord}
import api.RawKeyValueStore

class KeyValueStoreWritingConsumer(
  kafkaBootstrapServers: String,
  topic: String,
  groupId: String,
  store: RawKeyValueStore) extends Runnable {

  val running = new AtomicBoolean(true)

  def process(record: ConsumerRecord[Array[Byte], Array[Byte]]): Unit = {
    store.put(record.key, record.value)
  }

  override def run(): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaBootstrapServers)
    props.put("group.id", groupId)
    props.put("auto.offset.reset", "earliest") //we must replicate entire topic into local key-value store
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
    val consumer = new KafkaConsumer[Array[Byte], Array[Byte]](props)
    consumer.subscribe(List(topic))
    while (running.get) {
      val records = consumer.poll(100)
      for (record <- records) process(record)
    }
  }

  def close(): Unit = {
    running.set(false)
  }
}
