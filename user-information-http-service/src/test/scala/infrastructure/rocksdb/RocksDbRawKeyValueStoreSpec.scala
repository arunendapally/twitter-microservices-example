package infrastructure.rocksdb

import org.specs2.mutable.{Specification, After}
import org.specs2.specification.Scope
import org.specs2.concurrent.ExecutionEnv
import domain._
import scala.util.Random

class RocksDbRawKeyValueStoreSpec(implicit ee: ExecutionEnv) extends Specification {
  "RocksDB raw key value store" should {
    "return None when key is not found" in new context {
      store.get(bytes("key does not exist")) must beNone.await
    }

    "return value when key is found" in new context {
      store.put(key1, value1)
      store.get(key1) must beSome(value1).await
    }
  }

  trait context extends Scope with After {
    val testId = Random.nextInt(100000)
    val store = new RocksDbRawKeyValueStore(s"test$testId")
    def bytes(s: String): Array[Byte] = s.getBytes("UTF-8")
    val key1 = bytes("key1")
    val value1 = bytes("value1")

    override def after = {
      store.close()
      store.deleteFiles()
    }
  }
}
