package api

import domain.User

trait UserRepository {
  def createOrUpdateUser(user: User): Unit
}
