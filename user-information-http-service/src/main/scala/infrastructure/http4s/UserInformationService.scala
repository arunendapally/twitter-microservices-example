package infrastructure.http4s

import api.UserRepository
import core.ScalaFutureConverters._
import org.http4s._
import org.http4s.dsl._
import scalaz.concurrent.Task
import scala.concurrent.ExecutionContext.Implicits.global
import domain.User
import api.{Serializer, Deserializer}
import org.slf4j.LoggerFactory

trait UserInformationService extends UserRepository {
  private[this] val logger = LoggerFactory.getLogger(getClass)
  implicit def userIdSerializer: Serializer[String]
  implicit def userDeserializer: Deserializer[User]
  
  def userResponse(userId: String): Task[Response] = {
    logger.info(s"Looking up userId $userId")
    getUser(userId) flatMap { 
      case Some(user) => Ok(user)
      case None => NotFound()
    } asTask
  }

  lazy val userInformationService = HttpService {
    case GET -> Root / "users" / userId => 
      userResponse(userId)
  }
}
