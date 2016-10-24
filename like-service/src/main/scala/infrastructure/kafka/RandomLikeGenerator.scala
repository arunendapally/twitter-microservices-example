package infrastructure.kafka

import org.apache.kafka.streams.processor.{AbstractProcessor, ProcessorContext}
import domain.{User, Follow}
import org.slf4j.LoggerFactory
import scala.collection.mutable
import scala.util.Random
import java.util.UUID.randomUUID
import java.util.Date

/*
on user: add userId to buffer
on tweet: add tweetId to buffer
on punctuation: 

this is a join on 2 streams:
  1. tweets
  2. users
don't need to join records on same keys
just want to keep some tweets and some users, and then generate likes from them
more realistic:
  - store all users
  - store n recent tweets
  - periodically, for each tweet:
    - choose m random users, generate like for each user & tweet
we don't need realistic data, just some likes
  - store n most recently updated users
  - store n most recent tweets
  - periodically, generate likes between stored users & tweets
    - can even add randomness: flip coin to see if user likes tweet
each Processor is meant to work with specific (K, V) types
  - but we have 2 Vs: User and Tweet
  - kafka-streams join stuff seems to use multiple Processors

*/

class RandomLikeGenerator() extends AbstractProcessor[String, User] {
  private[this] val logger = LoggerFactory.getLogger(getClass)
  private[this] val userIds = mutable.Set.empty[String]
  private[this] val tweetIds = mutable.Set.empty[String]
  val userIdsCapacity = 10
  val tweetIdsCapacity = 10
  val likeProbability = 0.5
  val punctuationSchedule = 5000

  override def init(context: ProcessorContext): Unit = {
    super.init(context)
    context.schedule(punctuationSchedule)
    logger.debug(s"Scheduled punctuations every $punctuationSchedule msec")
  }

  override def process(userId: String, user: User): Unit = {
    userIds.add(userId)
  }

  override def punctuate(timestamp: Long): Unit = logTime("punctuate") {
    // completeSets()
    randomPairs()
    //TODO reduce userIds down to some fixed size so it doesn't grow unbounded
  }

  def randomPairs(): Unit = {
    val percentOfUsers = 0.05
    val chosenUserIds = mutable.ListBuffer.empty[String]
    for (userId <- userIds; if Random.nextDouble < percentOfUsers) {
      chosenUserIds += userId
    }
    var count = 0
    for {
      pair <- chosenUserIds.grouped(2)
      followerId <- pair.headOption
      followeeId <- pair.lastOption
    } {
      val follow = new Follow(randomUUID.toString, followerId, followeeId, new Date().getTime)
      context.forward(follow.getFollowId, follow)
      count += 1
    }
    logger.debug(s"Generated $count new follows")
  }

  def completeSets(): Unit = {
    val percentOfUsers = 0.05
    val followerIds = mutable.Set.empty[String]
    val followeeIds = mutable.Set.empty[String]
    for (userId <- userIds; if Random.nextDouble < percentOfUsers) {
      if (Random.nextDouble < 0.5) followerIds.add(userId)
      else followeeIds.add(userId)
    }
    var count = 0
    for (followerId <- followerIds; followeeId <- followeeIds) {
      val follow = new Follow(randomUUID.toString, followerId, followeeId, new Date().getTime)
      context.forward(follow.getFollowId, follow)
      count += 1
    }
    logger.debug(s"Generated $count new follows")
  }

  // override def close(): Unit = ???

  //TODO refactor this to core or something
  def logTime[T](description: String)(f: => T): T = {
    val t1 = System.currentTimeMillis
    val t = f
    val t2 = System.currentTimeMillis
    logger.debug(s"$description took ${t2 - t1} msec")
    t
  }
}
