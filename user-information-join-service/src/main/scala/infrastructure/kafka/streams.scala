package infrastructure.kafka

import java.lang.{Long => JLong}
import org.apache.kafka.streams.kstream.{KStreamBuilder, KStream, KTable}
import domain._

object UserInformationJoinService {
  def build(
      usersTopic: String, 
      tweetsTopic: String, 
      userInformationTopic: String,
      builder: KStreamBuilder): Unit = {
    val usersByUserId: KTable[String, User] = builder.table(usersTopic)

    val tweetsByTweetId: KStream[String, Tweet] = builder.stream(tweetsTopic)
    val extractUserId = (tweetId: String, tweet: Tweet) => tweet.getUserId
    val tweetsByUserId: KStream[String, Tweet] = tweetsByTweetId.selectKey(extractUserId) //we repartitioned by a different key, does this automatically go through a new kafka topic?
    val toLong = (l: JLong) => l.toLong
    val tweetCountsByUserId: KTable[String, Long] = tweetsByUserId.countByKey("tweetCountsByUserId").mapValues(toLong) //if mapValues is a performance hit, could just use JLong everywhere

    val userInformation: KTable[String, UserInformation] = usersByUserId.leftJoin(tweetCountsByUserId, UpdateFunctions.joinUserWithTweetCount)
    userInformation.to(userInformationTopic)
  }
}
