package api

import domain.Tweet

trait TweetRepository {
  def createTweet(tweet: Tweet): Unit
}
