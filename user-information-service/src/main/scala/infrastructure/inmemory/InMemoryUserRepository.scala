package infrastructure

import domain._
import api._
import scala.concurrent.Future

trait InMemoryUserRepository extends UserRepository {
  private[this] val users = scala.collection.concurrent.TrieMap.empty[String, User]

  def createOrUpdateUser(userId: String, create: => User, update: User => User): Unit = 
    users.update(userId, users.get(userId).map(update).getOrElse(create))

  def updateUserFields(userId: String, username: String): Unit = 
    createOrUpdateUser(userId, User.fromUserFields(userId, username), _.withUserFields(username))

  def updateTweetCount(userId: String, amount: Int): Unit = 
    createOrUpdateUser(userId, User.fromTweetCount(userId, amount), u => u.withTweetCount(u.tweetCount + amount))

  def getUser(userId: String): Future[Option[User]] = Future.successful(users.get(userId))
}
