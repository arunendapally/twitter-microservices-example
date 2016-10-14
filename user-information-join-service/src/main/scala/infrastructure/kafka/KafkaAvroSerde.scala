package infrastructure.kafka

import org.apache.kafka.common.serialization.{Serializer, Deserializer, Serde}
import io.confluent.kafka.serializers.{KafkaAvroSerializer, KafkaAvroDeserializer}
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import java.util.{Map => JMap, HashMap => JHashMap}

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

class SpecificKafkaAvroSerde extends KafkaAvroSerde {
  override def configure(configs: JMap[String, _], isKey: Boolean): Unit = {
    val effectiveConfigs = new JHashMap[String, Any](configs)
    effectiveConfigs.put(SPECIFIC_AVRO_READER_CONFIG, true)
    serializer.configure(effectiveConfigs, isKey)
    deserializer.configure(effectiveConfigs, isKey)
  }
}
