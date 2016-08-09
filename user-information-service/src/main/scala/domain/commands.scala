package domain

//not sure if these are needed or not.....

case class UpdateUserFields(
  userId: String,
  username: String) //TODO other fields

case class UpdateTweetCount(userId: String, change: Int) //could use single Update command, or separate Increment/Decrement commands

case class IncrementTweetCount(userId: String)
case class DecrementTweetCount(userId: String)

case class IncrementFollowingCount(userId: String)
case class DecrementFollowingCount(userId: String)

case class IncrementFollowerCount(userId: String)
case class DecrementFollowerCount(userId: String)

case class IncrementLikeCount(userId: String)
case class DecrementLikeCount(userId: String)
