package api

import scala.concurrent.{Future, ExecutionContext}
import domain.User

trait RawKeyValueStore {
  def get(key: Array[Byte]): Future[Option[Array[Byte]]]
  def put(key: Array[Byte], value: Array[Byte]): Unit
}

trait Serializer[T] {
  def toBytes(t: T): Array[Byte]
}

trait Deserializer[T] {
  def fromBytes(bytes: Array[Byte]): T //capture errors in type?
}

trait KeyValueLookup[K, V] {
  def raw: RawKeyValueStore

  def get(key: K)(implicit keySerializer: Serializer[K], valueDeserializer: Deserializer[V], ec: ExecutionContext): Future[Option[V]] = 
    raw.get(keySerializer.toBytes(key)).map(_.map(valueDeserializer.fromBytes))
}

trait UserRepository extends KeyValueLookup[String, User] {
  def getUser(userId: String)(implicit userIdSerializer: Serializer[String], userDeserializer: Deserializer[User], ec: ExecutionContext): Future[Option[User]] = get(userId)
}
