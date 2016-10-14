package infrastructure.t4j

import twitter4j.{StatusListener, Status, TwitterStreamFactory, TwitterStream, StatusDeletionNotice, StallWarning}
import twitter4j.conf.ConfigurationBuilder
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

  def oauthConsumerKey: String
  def oauthConsumerSecret: String
  def oauthAccessToken: String
  def oauthAccessTokenSecret: String

  def startTwitterStream(): TwitterStream = {
    val twitter4jConfiguration = new ConfigurationBuilder()
      .setOAuthConsumerKey(oauthConsumerKey)
      .setOAuthConsumerSecret(oauthConsumerSecret)
      .setOAuthAccessToken(oauthAccessToken)
      .setOAuthAccessTokenSecret(oauthAccessTokenSecret)
      .build()
    val twitterStream = new TwitterStreamFactory(twitter4jConfiguration).getInstance()
    twitterStream.addListener(this)
    twitterStream.sample()
    twitterStream
  }
}
