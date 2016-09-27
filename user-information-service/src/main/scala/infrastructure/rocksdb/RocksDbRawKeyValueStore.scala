package infrastructure.rocksdb

import api._
import scala.concurrent.Future
import org.rocksdb.{RocksDB, Options}
import org.apache.commons.io.FileUtils
import java.io.File

object RocksDbRawKeyValueStore {
  lazy val init = {
    RocksDB.loadLibrary()
  }
}

class RocksDbRawKeyValueStore(name: String) extends RawKeyValueStore {
  RocksDbRawKeyValueStore.init

  val options = new Options().setCreateIfMissing(true)
  def dir(name: String): String = name
  val db = RocksDB.open(options, dir(name))

  def close(): Unit = {
    db.close()
    options.dispose()
  }

  def deleteFiles(): Unit = {
    FileUtils.deleteDirectory(new File(dir(name)))
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  override def get(key: Array[Byte]): Future[Option[Array[Byte]]] = Future { Option(db.get(key)) }
  override def put(key: Array[Byte], value: Array[Byte]): Unit = Future { db.put(key, value) }
}
