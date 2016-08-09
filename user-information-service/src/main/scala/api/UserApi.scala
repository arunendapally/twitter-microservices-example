package api

import domain._
import scala.concurrent.Future

/*
Defines methods for looking up a user, and methods for updating user information.
Should these be separate traits or combined in a single trait?
  - separate traits will be impl by common code based on storage tech
  - traits would be used by separate things:
    - consume event streams => use write methods, not read methods
    - http handler => use read methods, not write methods
This is essentially Repository pattern...
Infrastucture layer contains:
  - impl tied to specific storage technology
  - consume event streams and use that impl to update user info
  - use HTTP/JSON lib to provide public API and use that impl to get user info
*/

// trait UserApi {
//   def updateUserFields(userId: String, username: String): Unit //TODO other fields

//   //could use single update command, or separate increment/decrement commands
//   def updateTweetCount(userId: String, amount: Int): Unit
//   def incrementTweetCount(userId: String): Unit
//   def decrementTweetCount(userId: String): Unit

//   def getUser(userId: String): Future[Option[User]]
// }

trait UserReadRepository {
  def getUser(userId: String): Future[Option[User]]
}

trait UserWriteRepository {
  def updateUserFields(userId: String, username: String): Unit //TODO other fields

  //could use single update command, or separate increment/decrement commands
  def updateTweetCount(userId: String, amount: Int): Unit
  def incrementTweetCount(userId: String): Unit
  def decrementTweetCount(userId: String): Unit
}

trait UserRepository extends UserReadRepository with UserWriteRepository
