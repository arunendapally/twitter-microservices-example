package domain 

import twitter4j.Status

case class FakeStatus(
  tweetId: Long,
  text: String,
  createdAt: java.util.Date,
  user: twitter4j.User) extends Status {
  // Members declared in java.lang.Comparable
  def compareTo(x$1: twitter4j.Status): Int = ???
  
  // Members declared in twitter4j.EntitySupport
  def getExtendedMediaEntities(): Array[twitter4j.ExtendedMediaEntity] = ???
  def getHashtagEntities(): Array[twitter4j.HashtagEntity] = ???
  def getMediaEntities(): Array[twitter4j.MediaEntity] = ???
  def getSymbolEntities(): Array[twitter4j.SymbolEntity] = ???
  def getURLEntities(): Array[twitter4j.URLEntity] = ???
  def getUserMentionEntities(): Array[twitter4j.UserMentionEntity] = ???
  
  // Members declared in twitter4j.Status
  def getContributors(): Array[Long] = ???
  def getCreatedAt(): java.util.Date = createdAt
  def getCurrentUserRetweetId(): Long = ???
  def getFavoriteCount(): Int = ???
  def getGeoLocation(): twitter4j.GeoLocation = ???
  def getId(): Long = tweetId
  def getInReplyToScreenName(): String = ???
  def getInReplyToStatusId(): Long = ???
  def getInReplyToUserId(): Long = ???
  def getLang(): String = ???
  def getPlace(): twitter4j.Place = ???
  def getQuotedStatus(): twitter4j.Status = ???
  def getQuotedStatusId(): Long = ???
  def getRetweetCount(): Int = ???
  def getRetweetedStatus(): twitter4j.Status = ???
  def getScopes(): twitter4j.Scopes = ???
  def getSource(): String = ???
  def getText(): String = text
  def getUser(): twitter4j.User = user
  def getWithheldInCountries(): Array[String] = ???
  def isFavorited(): Boolean = ???
  def isPossiblySensitive(): Boolean = ???
  def isRetweet(): Boolean = ???
  def isRetweeted(): Boolean = ???
  def isRetweetedByMe(): Boolean = ???
  def isTruncated(): Boolean = ???
  
  // Members declared in twitter4j.TwitterResponse
  def getAccessLevel(): Int = ???
  def getRateLimitStatus(): twitter4j.RateLimitStatus = ???
}
