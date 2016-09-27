package infrastructure.kafka

import org.specs2.mutable.{Specification, After}
import org.specs2.specification.Scope
import org.specs2.concurrent.ExecutionEnv
import infrastructure.inmemory.InMemoryRawKeyValueStore

import kafka.zk.EmbeddedZookeeper
import org.I0Itec.zkclient.ZkClient
import kafka.utils.{ZkUtils, ZKStringSerializer$, MockTime, TestUtils}
import kafka.server.KafkaConfig
import kafka.admin.{AdminUtils, RackAwareMode}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import java.nio.file.Files

class KeyValueStoreWritingConsumerSpec(implicit ee: ExecutionEnv) extends Specification {
  "key-value store writing consumer" should {
    "write received messages directly to raw key-value store" in new context {
      Thread.sleep(3000) //TODO replace this with broker live check
      producer.send(record(key1, value1))
      producer.send(record(key2, value2))
      Thread.sleep(3000) //TODO replace this with retries/eventually/whatever below
      raw.get(key1) must beSome(value1).await
      raw.get(key2) must beSome(value2).await
    }
  }

  trait context extends Scope with After {
    //https://gist.github.com/asmaier/6465468
    val zkHost = "127.0.0.1"
    val brokerHost = "127.0.0.1"
    val brokerPort = "9092"
    val topic = "user-information"

    // setup Zookeeper
    val zkServer = new EmbeddedZookeeper()
    val zkConnect = zkHost + ":" + zkServer.port
    val zkClient = new ZkClient(zkConnect, 30000, 30000, ZKStringSerializer$.MODULE$)
    val zkUtils = ZkUtils.apply(zkClient, false)

    // setup Broker
    val brokerProps = new Properties()
    brokerProps.setProperty("zookeeper.connect", zkConnect)
    brokerProps.setProperty("broker.id", "0")
    val logDirs = Files.createTempDirectory("kafka-").toAbsolutePath().toString()
    println(s"logDirs = $logDirs")
    brokerProps.setProperty("log.dirs", logDirs)
    brokerProps.setProperty("listeners", "PLAINTEXT://" + brokerHost +":" + brokerPort)
    val config = new KafkaConfig(brokerProps)
    val mock = new MockTime()
    val kafkaServer = TestUtils.createServer(config, mock)

    // create topic
    AdminUtils.createTopic(zkUtils, topic, 1, 1, new Properties(), RackAwareMode.Disabled)

     // setup producer
    val producerProps = new Properties()
    producerProps.setProperty("bootstrap.servers", brokerHost + ":" + brokerPort)
    producerProps.setProperty("key.serializer","org.apache.kafka.common.serialization.ByteArraySerializer")
    producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    val producer = new KafkaProducer[Array[Byte], Array[Byte]](producerProps)

    val raw = new InMemoryRawKeyValueStore {}
    val consumer = new KeyValueStoreWritingConsumer(s"$brokerHost:$brokerPort", topic, "user-information-http-service", raw)
    new Thread(consumer).start()

    def bytes(s: String): Array[Byte] = s.getBytes("UTF-8")
    val key1 = bytes("key1")
    val key2 = bytes("key2")
    val value1 = bytes("value1")
    val value2 = bytes("value2")

    def record(key: Array[Byte], value: Array[Byte]): ProducerRecord[Array[Byte], Array[Byte]] = 
      new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)

    def after = {
      //TODO something is not getting cleaned up properly here, sometimes after 1st test run we get this: kafka.common.TopicExistsException: topic user-information already exists (AdminUtils.scala:444)
      consumer.close()
      producer.close()
      kafkaServer.shutdown()
      zkClient.close()
      zkServer.shutdown()
      println("closed everything")
    }
  }
}
