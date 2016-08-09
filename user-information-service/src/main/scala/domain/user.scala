package domain

final case class UserId(val id: String) extends AnyVal //necessary?

case class User(
    userId: String,
    username: Option[String] = None,
    tweetCount: Int = 0) {
  def withUserFields(username: String): User = copy(username = Some(username))
  def withTweetCount(tweetCount: Int): User = copy(tweetCount = tweetCount)
}

object User {
  def fromUserFields(userId: String, username: String): User = User(userId, Some(username))
  def fromTweetCount(userId: String, tweetCount: Int): User = User(userId, tweetCount = tweetCount)
}
