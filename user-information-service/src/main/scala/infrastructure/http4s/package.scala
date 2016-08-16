package infrastructure

import domain.User
import org.http4s.circe.CirceInstances
import io.circe._
import io.circe.generic.auto._

package object http4s extends CirceInstances {
  implicit val userEntityDecoder = jsonOf[User]
  implicit val userEntityEncoder = jsonEncoderOf[User]
}
