package infrastructure.http4s

import api.UserReadRepository
import core.ScalaFutureConverters._
import org.http4s._
import org.http4s.dsl._
import scalaz.concurrent.Task
import scala.concurrent.ExecutionContext.Implicits.global

trait UserInformationService extends UserReadRepository {
  def userResponse(userId: String): Task[Response] = 
    getUser(userId) flatMap { 
      case Some(user) => Ok(user)
      case None => NotFound()
    } asTask

  lazy val userInformationService = HttpService {
    case GET -> Root / "users" / userId => 
      userResponse(userId)
  }
}
