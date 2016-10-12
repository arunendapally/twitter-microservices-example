package infrastructure

import twitter4j.TwitterStreamFactory
import twitter4j.conf.ConfigurationBuilder
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import io.confluent.kafka.serializers.{KafkaAvroSerializer, AbstractKafkaAvroSerDeConfig}
import org.apache.avro.specific.SpecificRecord
import java.util.Properties
import domain._
import infrastructure.kafka.KafkaRepository
import infrastructure.t4j.Twitter4jListener
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object Main extends App with KafkaRepository with Twitter4jListener {
  val config = ConfigFactory.load()
  val logger = LoggerFactory.getLogger(getClass)

  val tweetsTopic = config.getString("tweet-service.tweets-topic")
  val usersTopic = config.getString("tweet-service.users-topic")

  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("tweet-service.kafka-bootstrap-servers"))
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getString("tweet-service.schema-registry-url"))
  val producer = new KafkaProducer[String, SpecificRecord](props)

  val twitter4jConfiguration = new ConfigurationBuilder()
    .setOAuthConsumerKey(config.getString("twitter.oauth.consumer-key"))
    .setOAuthConsumerSecret(config.getString("twitter.oauth.consumer-secret"))
    .setOAuthAccessToken(config.getString("twitter.oauth.access-token"))
    .setOAuthAccessTokenSecret(config.getString("twitter.oauth.access-token-secret"))
    .build()

  val twitterStream = new TwitterStreamFactory(twitter4jConfiguration).getInstance()
  twitterStream.addListener(this)
  twitterStream.sample()

  scala.sys.addShutdownHook {
    twitterStream.shutdown()
    producer.close()
    logger.info("Shutting down")
  }
}
