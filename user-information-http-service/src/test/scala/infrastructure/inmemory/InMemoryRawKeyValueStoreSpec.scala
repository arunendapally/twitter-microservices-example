package infrastructure.inmemory

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.concurrent.ExecutionEnv
import domain._

class InMemoryRawKeyValueStoreSpec(implicit ee: ExecutionEnv) extends Specification {
  "in-memory raw key value store" should {
    "return None when key not found" in new context {
      store.get(bytes("key does not exist")) must beNone.await
    }

    "return value when key is found" in new context {
      store.put(key1, value1)
      store.get(key1) must beSome(value1).await
    }
  }

  trait context extends Scope {
    val store = new InMemoryRawKeyValueStore {}
    def bytes(s: String): Array[Byte] = s.getBytes("UTF-8")
    val key1 = bytes("key1")
    val value1 = bytes("value1")
  }
}
