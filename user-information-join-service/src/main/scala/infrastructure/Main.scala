package infrastructure

import java.util.Properties
import java.lang.{Long => JLong}
import org.apache.kafka.streams.{StreamsConfig, KafkaStreams}
import org.apache.kafka.streams.kstream.{KStreamBuilder, KStream, KTable, KeyValueMapper}
import com.typesafe.config.ConfigFactory
import domain.Tweet

object Main extends App {

  //TODO refactor this to core package
  import scala.language.implicitConversions
  implicit def functionToKeyValueMapper[K, V, R](f: (K, V) => R): KeyValueMapper[K, V, R] = new KeyValueMapper[K, V, R] {
    override def apply(key: K, value: V): R = f(key, value)
  }

  val config = ConfigFactory.load()

  val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getString("service.application-id"))
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("service.kafka-bootstrap-servers"))
  props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, config.getString("service.zookeeper-connect"))
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getString("service.schema-registry-url"))
  props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, ???)
  props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, ???)
  val streamsConfig = new StreamsConfig(props)

  val builder = new KStreamBuilder

  val tweetsTopic = config.getString("service.tweetsTopic")
  val tweets: KStream[String, Tweet] = builder.stream(tweetsTopic)
  val extractUserId = (tweetId: String, tweet: Tweet) => tweet.getUserId
  val tweetsByUserId: KStream[String, Tweet] = tweets.selectKey(extractUserId) //we repartitioned by a different key, does this automatically go through a new kafka topic?
  val tweetCountsByUserId: KTable[String, JLong] = tweetsByUserId.countByKey("tweetCountsByUserId")

  val streams = new KafkaStreams(builder, streamsConfig)
  streams.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
    override def uncaughtException(thread: Thread, e: Throwable) {
        //TODO log error
    }
  })
  streams.start()

  scala.sys.addShutdownHook {
    streams.close()
  }
}
