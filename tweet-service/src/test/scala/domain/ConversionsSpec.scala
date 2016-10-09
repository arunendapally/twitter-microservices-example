package domain

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import java.util.Date

object ConversionsSpec extends Specification {
  "Conversions" should {
    "split a Status into a Tweet and a user" in new context {
      val (tweet, user) = Conversions.fromStatus(fakeStatus)
      tweet.getTweetId must_== tweetId.toString
      tweet.getText must_== text
      tweet.getCreatedAt must_== createdAt.getTime
      tweet.getUserId must_== userId.toString
      user.getUserId must_== userId.toString
      user.getUsername must_== username
      user.getName must_== name
      user.getDescription must_== description
    }
  }

  trait context extends Scope {
    val tweetId = 1
    val text = "This is a tweet"
    val createdAt = new Date()
    val userId = 2
    val username = "user1"
    val name = "The User 1"
    val description = "This is user number 1"
    val fakeUser = FakeUser(userId, username, name, description)
    val fakeStatus = FakeStatus(tweetId, text, createdAt, fakeUser)
  }
}
