package infrastructure.http4s

import api._
import core.ScalaFutureConverters._
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import scalaz.concurrent.Task
import scala.concurrent.ExecutionContext.Implicits.global //used in Future.map below...

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

trait Http4sService extends ServerApp with UserInformationService {
  override def server(args: List[String]): Task[Server] = 
    BlazeBuilder
      .bindHttp(8080, "localhost") //TODO from configs
      .mountService(userInformationService, "/api")
      .start
}
