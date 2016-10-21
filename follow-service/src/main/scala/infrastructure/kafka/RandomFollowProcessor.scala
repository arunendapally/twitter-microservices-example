package infrastructure.kafka

import org.apache.kafka.streams.processor.{AbstractProcessor, ProcessorContext}
import domain.{User, Follow}
import org.slf4j.LoggerFactory
import scala.collection.mutable
import scala.util.Random
import java.util.UUID.randomUUID
import java.util.Date

class RandomFollowProcessor() extends AbstractProcessor[String, User] {
  private[this] val logger = LoggerFactory.getLogger(getClass)
  private[this] val userIds = mutable.Set.empty[String]
  val punctuationSchedule = 5000

  override def init(context: ProcessorContext): Unit = {
    super.init(context)
    context.schedule(punctuationSchedule)
    logger.debug(s"Scheduled punctionations every $punctuationSchedule msec")
  }

  override def process(userId: String, user: User): Unit = {
    userIds.add(userId)
  }

  override def punctuate(timestamp: Long): Unit = logTime("punctuate") {
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
    //TODO reduce userIds down to some fixed size so it doesn't grow unbounded
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
