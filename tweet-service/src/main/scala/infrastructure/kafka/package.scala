package infrastructure

import org.apache.kafka.clients.producer.ProducerRecord
import scala.language.implicitConversions

package object kafka {
  implicit def tuple3ToProducerRecord[K, V](tuple: (String, K, V)): ProducerRecord[K, V] = {
    val (topic, key, value) = tuple
    new ProducerRecord[K, V](topic, key, value)
  }
}
