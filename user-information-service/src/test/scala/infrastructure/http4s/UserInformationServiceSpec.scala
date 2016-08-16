package infrastructure.http4s

import infrastructure.inmemory.InMemoryUserRepository
import domain.User
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.http4s._
import org.http4s.dsl._

object UserInformationServiceSpec extends Specification with org.specs2.matcher.TaskMatchers {
  "the user information service" should {
    "respond 404 when user is not found" in new context {
      val response = serve(Request(GET, Uri(path = "/users/doesnotexist")))
      response.status must_== NotFound
      response.as[String] must returnValue("")
    }

    "respond 200 with user json when user is found" in new context {
      val response = serve(Request(GET, Uri(path = "/users/user1")))
      response.status must_== Ok
      response.as[User] must returnValue(User.fromUserFields("user1", "user1").withTweetCount(1))
    }
  }

  trait context extends Scope with UserInformationService with InMemoryUserRepository {
    def serve(request: Request): Response =
      userInformationService.run(request).run

    updateUserFields("user1", "user1")
    updateTweetCount("user1", 1)
  }
}
