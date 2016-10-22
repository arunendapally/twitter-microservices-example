package domain

// final case class UserId(val id: String) extends AnyVal //necessary?

case class User(
    userId: String,
    username: Option[String] = None,
    tweetCount: Long = 0,
    followerCount: Long = 0,
    followingCount: Long = 0) {
  def withUserFields(username: String): User = copy(username = Some(username))
  def withTweetCount(tweetCount: Long): User = copy(tweetCount = tweetCount)
  def withFollowerCount(followerCount: Long): User = copy(followerCount = followerCount)
  def withFollowingCount(followingCount: Long): User = copy(followingCount = followingCount)
}

object User {
  def fromUserFields(userId: String, username: String): User = User(userId, Some(username))
  def fromTweetCount(userId: String, tweetCount: Long): User = User(userId, tweetCount = tweetCount)
}
