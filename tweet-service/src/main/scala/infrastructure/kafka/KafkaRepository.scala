package infrastructure.kafka

import domain._
import api._
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.avro.specific.SpecificRecord

trait KafkaRepository extends TweetRepository with UserRepository {
  def producer: KafkaProducer[String, SpecificRecord]
  def tweetsTopic: String
  def usersTopic: String

  override def createTweet(tweet: Tweet): Unit = producer.send((tweetsTopic, tweet.getTweetId, tweet))
  override def createOrUpdateUser(user: User): Unit = producer.send((usersTopic, user.getUserId, user))
}
