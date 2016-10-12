package domain

object UpdateFunctions {

  val joinUserWithTweetCount: (User, Long) => UserInformation = (user, tweetCount) =>
    setFields(UserInformation.newBuilder, user)
      .setUserId(user.getUserId)
      .setTweetCount(tweetCount.toInt) //TODO maybe UserInformation.tweetCount shoudl be a Long?
      .build

  def setFields(builder: UserInformation.Builder, user: User): UserInformation.Builder = 
    builder
      .setUsername(user.getUsername)
      .setName(user.getName)
      .setDescription(user.getDescription)

  def createUserInformation(userId: String, user: User): UserInformation = 
    setFields(UserInformation.newBuilder, user).setUserId(userId).build

  def updateUserInformation(userInformation: UserInformation, userId: String, user: User): UserInformation = 
    setFields(UserInformation.newBuilder(userInformation), user).build

  def fromUser(maybeUserInformation: Option[UserInformation], userId: String, user: User): UserInformation = 
    maybeUserInformation match {
      case Some(info) => updateUserInformation(info, userId, user)
      case None => createUserInformation(userId, user)
    }

  def createUserInformation(userId: String, tweet: Tweet): UserInformation = 
    UserInformation.newBuilder
      .setUserId(userId)
      .setTweetCount(1)
      .build

  def updateUserInformation(userInformation: UserInformation, userId: String, tweet: Tweet): UserInformation = {
    // UserInformation.newBuilder(userInformation).setTweetCount(userInformation.getTweetCount + 1).build
    userInformation.setTweetCount(userInformation.getTweetCount + 1)
    userInformation
  }

  def fromTweet(maybeUserInformation: Option[UserInformation], userId: String, tweet: Tweet): UserInformation = 
    maybeUserInformation match {
      case Some(info) => updateUserInformation(info, userId, tweet)
      case None => createUserInformation(userId, tweet)
    }
}
