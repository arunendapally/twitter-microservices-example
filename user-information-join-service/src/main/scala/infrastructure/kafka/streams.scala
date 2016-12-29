package infrastructure.kafka

import java.lang.{Long => JLong}
import org.apache.kafka.streams.kstream.{KStreamBuilder, KStream, KTable}
import domain._
import scala.language.implicitConversions

object UserInformationJoinService {
  def build(
      usersTopic: String, 
      tweetsTopic: String, 
      followsTopic: String,
      userInformationTopic: String,
      builder: KStreamBuilder): Unit = {

    val tweetCounts: KTable[String, Long] = 
      builder
        .stream[String, Tweet](tweetsTopic)
        .selectKey((tweetId: String, tweet: Tweet) => tweet.getUserId)
        .groupByKey()
        .count("tweetCounts")

    val follows = builder.stream[String, Follow](followsTopic)

    val followingCounts: KTable[String, Long] = 
      follows
        .selectKey((followId: String, follow: Follow) => follow.getFollowerId)
        .groupByKey()
        .count("followingCounts")

    val followerCounts: KTable[String, Long] = 
      follows
        .selectKey((followId: String, follow: Follow) => follow.getFolloweeId)
        .groupByKey()
        .count("followerCounts")

    builder
      .table[String, User](usersTopic, "users")
      .mapValues((user: User) => UserInformation.newBuilder
        .setUserId(user.getUserId)
        .setUsername(user.getUsername)
        .setName(user.getName)
        .setDescription(user.getDescription)
        .build)
      .leftJoin(tweetCounts, { (userInformation: UserInformation, tweetCount: Long) => 
        userInformation.setTweetCount(tweetCount)
        userInformation })
      .leftJoin(followingCounts, { (userInformation: UserInformation, followingCount: Long) => 
        userInformation.setFollowingCount(followingCount)
        userInformation })
      .leftJoin(followerCounts, { (userInformation: UserInformation, follwoerCount: Long) => 
        userInformation.setFollowerCount(follwoerCount)
        userInformation })
      .to(userInformationTopic)
  }
}
