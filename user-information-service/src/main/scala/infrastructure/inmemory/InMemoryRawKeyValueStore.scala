package infrastructure.inmemory

import api._
import scala.concurrent.Future
import java.util.Arrays

/** Allows Array[Byte] to be used as keys in a map. */
class ByteArrayWrapper(val bytes: Array[Byte]) {
  override def equals(obj: Any): Boolean = 
    obj.isInstanceOf[ByteArrayWrapper] && Arrays.equals(bytes, obj.asInstanceOf[ByteArrayWrapper].bytes)

  override def hashCode(): Int = Arrays.hashCode(bytes)
}

trait InMemoryRawKeyValueStore extends RawKeyValueStore {
  private[this] val store = scala.collection.concurrent.TrieMap.empty[ByteArrayWrapper, Array[Byte]]

  def wrap(bytes: Array[Byte]) = new ByteArrayWrapper(bytes)

  override def get(key: Array[Byte]): Future[Option[Array[Byte]]] = Future.successful(store.get(wrap(key)))
  override def put(key: Array[Byte], value: Array[Byte]): Unit = store.update(wrap(key), value)
}
