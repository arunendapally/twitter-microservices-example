package infrastructure.http4s

import api._
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s.circe.CirceInstances
import scalaz.concurrent.Task
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global //used in Future.map below...

trait Http4sService extends UserReadRepository with ServerApp with CirceInstances {
	val userInfoService = HttpService {
    case GET -> Root / "users" / userId => 
      // Ok(s"You requested userId $userId")
      Ok(getUser(userId).map(_.asJson)) //TODO 404 if None
      // getUser(userId) map { 
      //   case Some(user) => Ok(user.asJson)
      //   case None => NotFound
      // }
  }

  override def server(args: List[String]): Task[Server] = 
    BlazeBuilder
      .bindHttp(8080, "localhost") //TODO from configs
      .mountService(userInfoService, "/api")
      .start
}
