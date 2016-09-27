package infrastructure.http4s

import infrastructure.inmemory.InMemoryRawKeyValueStore
import infrastructure.confluentschemaregistry._
import domain.User
import api._
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

  trait context extends Scope with UserInformationService {
    def serve(request: Request): Response =
      userInformationService.run(request).run

    override val raw = new InMemoryRawKeyValueStore {}

    override lazy val userIdSerializer: Serializer[String] = new TestConfluentAvroPrimitiveSerializer("test-users", true)
    override lazy val userDeserializer: Deserializer[User] = new TestConfluentGenericRecordDeserializer[User](false)(GenericRecordUserReader)
    lazy val userSerializer: Serializer[User] = new TestConfluentAvroGenericSerializer[User]("test-users", false)(GenericRecordUserWriter)

    val userId1 = "user1"
    val username1 = "user1"
    val user1 = User.fromUserFields(userId1, username1).withTweetCount(1)
    raw.put(userIdSerializer.toBytes(userId1), userSerializer.toBytes(user1))
  }
}
