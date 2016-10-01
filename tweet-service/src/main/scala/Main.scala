package infrastructure

import twitter4j.{StatusListener, Status, TwitterStreamFactory, StatusDeletionNotice, StallWarning}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.IndexedRecord
import java.util.Properties
import infrastructure.avro.{Tweet, User}

object Main extends App with StatusListener {
  val tweetsTopic = "tweets" //TODO from config
  val usersTopic = "users"
  val kafkaBootstrapServers = "192.168.99.100:9092"
  val schemaRegistryUrl = "http://192.168.99.100:8081"

  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  props.put("schema.registry.url", schemaRegistryUrl)
  val producer = new KafkaProducer[String, IndexedRecord](props)

  def toAvro(status: Status): (Tweet, User) = (
    Tweet.newBuilder
      .setTweetId(status.getId.toString)
      .setText(status.getText)
      .setCreatedAt(status.getCreatedAt.getTime)
      .build,
    User.newBuilder
      .setUserId(status.getUser.getId.toString)
      .setUsername(status.getUser.getScreenName)
      .setName(status.getUser.getName)
      .setDescription(status.getUser.getDescription)
      .build
  )

  implicit def tuple3ToProducerRecord[K, V](t: (String, K, V)): ProducerRecord[K, V] = new ProducerRecord[K, V](t._1, t._2, t._3)

  override def onStatus(status: Status): Unit = {
    val (tweet, user) = toAvro(status)
    producer.send((tweetsTopic, status.getId.toString, tweet))
    producer.send((usersTopic, status.getUser.getId.toString, user))
  }

  override def onDeletionNotice(notice: StatusDeletionNotice): Unit = ???
  override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = ???
  override def onStallWarning(warning: StallWarning): Unit = ???
  override def onTrackLimitationNotice(notice: Int): Unit = ???
  override def onException(e: Exception): Unit = ???

  val twitterStream = new TwitterStreamFactory().getInstance()
  twitterStream.addListener(this)
  twitterStream.sample()

  scala.sys.addShutdownHook {
    twitterStream.shutdown()
    producer.close()
  }
}
