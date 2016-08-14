package infrastructure.inmemory

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.concurrent.ExecutionEnv
import domain._

class InMemoryUserRepositorySpec(implicit ee: ExecutionEnv) extends Specification {
  "in-memory user repository" should {
    "return None when user does not exist" in new context {
      getUser("does not exist") must beNone.await
    }

    "update a user and return it" in new context {
      updateUserFields(userId1, username1)
      getUser(userId1) must beSome(User.fromUserFields(userId1, username1)).await
      updateUserFields(userId1, username1b)
      getUser(userId1) must beSome(User.fromUserFields(userId1, username1b)).await
    }

    "increment tweet count and return user" in new context {
      incrementTweetCount(userId1)
      getUser(userId1) must beSome(User.fromTweetCount(userId1, 1)).await
    }

    "increment tweet count after updating user" in new context {
      updateUserFields(userId1, username1)
      incrementTweetCount(userId1)
      incrementTweetCount(userId1)
      getUser(userId1) must beSome(User.fromUserFields(userId1, username1).withTweetCount(2)).await
    }

    "increment tweet count before updating user fields" in new context {
      incrementTweetCount(userId1)
      incrementTweetCount(userId1)
      updateUserFields(userId1, username1)
      getUser(userId1) must beSome(User.fromUserFields(userId1, username1).withTweetCount(2)).await
    }
  }

  trait context extends Scope with InMemoryUserRepository {
    val userId1 = "u1"
    val username1 = "userone"
    val username1b = "useronebee"
  }
}
