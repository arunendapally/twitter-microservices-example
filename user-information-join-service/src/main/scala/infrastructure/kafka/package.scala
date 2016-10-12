package infrastructure

import scala.language.implicitConversions
import org.apache.kafka.streams.kstream.{KeyValueMapper, ValueJoiner, ValueMapper}

package object kafka {
  implicit def functionToKeyValueMapper[K, V, R](f: (K, V) => R): KeyValueMapper[K, V, R] = new KeyValueMapper[K, V, R] {
    override def apply(key: K, value: V): R = f(key, value)
  }

  implicit def functionToValueJoiner[V1, V2, R](f: (V1, V2) => R): ValueJoiner[V1, V2, R] = new ValueJoiner[V1, V2, R] {
    override def apply(v1: V1, v2: V2): R = f(v1, v2)
  }

  implicit def functionToValueMapper[V1, V2](f: V1 => V2): ValueMapper[V1, V2] = new ValueMapper[V1, V2] {
    override def apply(v1: V1): V2 = f(v1)
  }
}
