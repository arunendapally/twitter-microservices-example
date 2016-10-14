package infrastructure.kafka

import org.apache.kafka.common.serialization.{Serializer, Deserializer, Serde}
import io.confluent.kafka.serializers.{KafkaAvroSerializer, KafkaAvroDeserializer}
import java.util.{Map => JMap}

class KafkaAvroSerde extends Serde[Object] {
  private[this] val kafkaAvroSerializer = new KafkaAvroSerializer
  private[this] val kafkaAvroDeserializer = new KafkaAvroDeserializer

  def deserializer(): Deserializer[Object] = kafkaAvroDeserializer
  def serializer(): Serializer[Object] = kafkaAvroSerializer

  def configure(configs: JMap[String, _], isKey: Boolean): Unit = {
    serializer.configure(configs, isKey)
    deserializer.configure(configs, isKey)
  }

  def close(): Unit = {
    serializer.close()
    deserializer.close()
  }
}