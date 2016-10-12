package domain

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

object UpdateFunctionsSpec extends Specification {
  "Update functions" should {
    "join a user and tweet count" in new context {
      val userInfo = UpdateFunctions.joinUserWithTweetCount(user, tweetCount)
      userInfo.getUserId must_== userId
      userInfo.getUsername must_== username
      userInfo.getName must_== name
      userInfo.getDescription must_== description
      userInfo.getTweetCount must_== tweetCount
    }
  }

  trait context extends Scope {
    val userId = "1"
    val username = "user1"
    val name = "The User 1"
    val description = "This is User 1"
    val user = new User(userId, username, name, description)
    val tweetCount = 10
  }
}
