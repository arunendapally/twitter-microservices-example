package infrastructure.t4j

import twitter4j.Status
import domain.{Tweet, User}

object Conversions {
  def fromStatus(status: Status): (Tweet, User) = (
    Tweet.newBuilder()
      .setTweetId(status.getId.toString)
      .setText(status.getText)
      .setCreatedAt(status.getCreatedAt.getTime)
      .setUserId(status.getUser.getId.toString)
      .build(),
    User.newBuilder()
      .setUserId(status.getUser.getId.toString)
      .setUsername(status.getUser.getScreenName)
      .setName(status.getUser.getName)
      .setDescription(status.getUser.getDescription)
      .build()
  )
}