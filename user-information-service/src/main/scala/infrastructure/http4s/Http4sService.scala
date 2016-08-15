package infrastructure.http4s

import api._
import core.ScalaFutureConverters._
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s.circe.CirceInstances
import scalaz.concurrent.Task
import scala.concurrent.Future
import domain.User
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global //used in Future.map below...

trait Http4sService extends UserReadRepository with ServerApp with CirceInstances {

  def userResponse(userId: String): Task[Response] = 
    getUser(userId) flatMap { 
      case Some(user) => Ok(user.asJson)
      case None => NotFound()
    } asTask

	val userInfoService = HttpService {
    case GET -> Root / "users" / userId => 
      userResponse(userId)
  }

  override def server(args: List[String]): Task[Server] = 
    BlazeBuilder
      .bindHttp(8080, "localhost") //TODO from configs
      .mountService(userInfoService, "/api")
      .start
}
