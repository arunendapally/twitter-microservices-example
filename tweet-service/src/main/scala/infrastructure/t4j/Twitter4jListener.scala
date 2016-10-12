package infrastructure.t4j

import twitter4j.{StatusListener, Status, TwitterStreamFactory, StatusDeletionNotice, StallWarning}
import api.{TweetRepository, UserRepository}

trait Twitter4jListener extends StatusListener with TweetRepository with UserRepository {
  override def onStatus(status: Status): Unit = {
    val (tweet, user) = Conversions.fromStatus(status)
    createTweet(tweet)
    createOrUpdateUser(user)
  }

  override def onDeletionNotice(notice: StatusDeletionNotice): Unit = {}
  override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}
  override def onStallWarning(warning: StallWarning): Unit = {}
  override def onTrackLimitationNotice(notice: Int): Unit = {}
  override def onException(e: Exception): Unit = {}
}
