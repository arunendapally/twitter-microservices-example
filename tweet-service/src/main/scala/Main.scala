package infrastructure

import twitter4j.{StatusListener, Status, TwitterStreamFactory, StatusDeletionNotice, StallWarning}
import twitter4j.conf.ConfigurationBuilder
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import io.confluent.kafka.serializers.{KafkaAvroSerializer, AbstractKafkaAvroSerDeConfig}
import org.apache.avro.generic.IndexedRecord
import org.apache.avro.specific.SpecificRecord
import java.util.Properties
import infrastructure.avro.{Tweet, User}
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import scala.language.implicitConversions

object Main extends App with StatusListener {
  val config = ConfigFactory.load()
  val logger = LoggerFactory.getLogger(getClass)

  val tweetsTopic = config.getString("tweet-service.tweets-topic")
  val usersTopic = config.getString("tweet-service.users-topic")
  val kafkaBootstrapServers = config.getString("tweet-service.kafka-bootstrap-servers")

  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getString("tweet-service.schema-registry-url"))
  val producer = new KafkaProducer[String, SpecificRecord](props)

  def toAvro(status: Status): (Tweet, User) = (
    Tweet.newBuilder()
      .setTweetId(status.getId.toString)
      .setText(status.getText)
      .setCreatedAt(status.getCreatedAt.getTime)
      .setUserId(status.getUser.getId.toString)
      .build(),
    User.newBuilder()
      .setUserId(status.getUser.getId.toString)
      .setUsername(status.getUser.getScreenName)
      .setName(status.getUser.getName)
      .setDescription(status.getUser.getDescription)
      .build()
  )

  implicit def tuple3ToProducerRecord[K, V](tuple: (String, K, V)): ProducerRecord[K, V] = {
    val (topic, key, value) = tuple
    new ProducerRecord[K, V](topic, key, value)
  }

  override def onStatus(status: Status): Unit = {
    val (tweet, user) = toAvro(status)
    val tweetResult = producer.send(new ProducerRecord[String, SpecificRecord](tweetsTopic, tweet.getTweetId, tweet))
    val userResult = producer.send(new ProducerRecord[String, SpecificRecord](usersTopic, user.getUserId, user))
  }

  override def onDeletionNotice(notice: StatusDeletionNotice): Unit = {}
  override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}
  override def onStallWarning(warning: StallWarning): Unit = {}
  override def onTrackLimitationNotice(notice: Int): Unit = {}
  override def onException(e: Exception): Unit = {}

  val twitter4jConfiguration = new ConfigurationBuilder()
    .setOAuthConsumerKey(config.getString("twitter.oauth.consumer-key"))
    .setOAuthConsumerSecret(config.getString("twitter.oauth.consumer-secret"))
    .setOAuthAccessToken(config.getString("twitter.oauth.access-token"))
    .setOAuthAccessTokenSecret(config.getString("twitter.oauth.access-token-secret"))
    .build()

  val twitterStream = new TwitterStreamFactory(twitter4jConfiguration).getInstance()
  twitterStream.addListener(this)
  twitterStream.sample()

  logger.info(s"Consuming from Twitter Streaming API and writing to Kafka at $kafkaBootstrapServers...")
  logger.info(s"Tweets are written to the $tweetsTopic topic")
  logger.info(s"Users are written to the $usersTopic topic")

  scala.sys.addShutdownHook {
    twitterStream.shutdown()
    producer.close()
    logger.info("Shutting down")
  }
}
