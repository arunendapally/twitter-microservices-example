package domain

case class FakeUser(
    userId: Long,
    username: String,
    name: String,
    description: String) extends twitter4j.User {
  // Members declared in java.lang.Comparable
  def compareTo(x$1: twitter4j.User): Int = ???
  
  // Members declared in twitter4j.TwitterResponse
  def getAccessLevel(): Int = ???
  def getRateLimitStatus(): twitter4j.RateLimitStatus = ???
  
  // Members declared in twitter4j.User
  def getBiggerProfileImageURL(): String = ???
  def getBiggerProfileImageURLHttps(): String = ???
  def getCreatedAt(): java.util.Date = ???
  def getDescription(): String = description
  def getDescriptionURLEntities(): Array[twitter4j.URLEntity] = ???
  def getEmail(): String = ???
  def getFavouritesCount(): Int = ???
  def getFollowersCount(): Int = ???
  def getFriendsCount(): Int = ???
  def getId(): Long = userId
  def getLang(): String = ???
  def getListedCount(): Int = ???
  def getLocation(): String = ???
  def getMiniProfileImageURL(): String = ???
  def getMiniProfileImageURLHttps(): String = ???
  def getName(): String = name
  def getOriginalProfileImageURL(): String = ???
  def getOriginalProfileImageURLHttps(): String = ???
  def getProfileBackgroundColor(): String = ???
  def getProfileBackgroundImageURL(): String = ???
  def getProfileBackgroundImageUrlHttps(): String = ???
  def getProfileBannerIPadRetinaURL(): String = ???
  def getProfileBannerIPadURL(): String = ???
  def getProfileBannerMobileRetinaURL(): String = ???
  def getProfileBannerMobileURL(): String = ???
  def getProfileBannerRetinaURL(): String = ???
  def getProfileBannerURL(): String = ???
  def getProfileImageURL(): String = ???
  def getProfileImageURLHttps(): String = ???
  def getProfileLinkColor(): String = ???
  def getProfileSidebarBorderColor(): String = ???
  def getProfileSidebarFillColor(): String = ???
  def getProfileTextColor(): String = ???
  def getScreenName(): String = username
  def getStatus(): twitter4j.Status = ???
  def getStatusesCount(): Int = ???
  def getTimeZone(): String = ???
  def getURL(): String = ???
  def getURLEntity(): twitter4j.URLEntity = ???
  def getUtcOffset(): Int = ???
  def getWithheldInCountries(): Array[String] = ???
  def isContributorsEnabled(): Boolean = ???
  def isDefaultProfile(): Boolean = ???
  def isDefaultProfileImage(): Boolean = ???
  def isFollowRequestSent(): Boolean = ???
  def isGeoEnabled(): Boolean = ???
  def isProfileBackgroundTiled(): Boolean = ???
  def isProfileUseBackgroundImage(): Boolean = ???
  def isProtected(): Boolean = ???
  def isShowAllInlineMedia(): Boolean = ???
  def isTranslator(): Boolean = ???
  def isVerified(): Boolean = ???
}