package infrastructure.http4s

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import scalaz.concurrent.Task

trait Http4sService extends ServerApp with UserInformationService {
  override def server(args: List[String]): Task[Server] = 
    BlazeBuilder
      .bindHttp(8080, "localhost") //TODO from configs
      .mountService(userInformationService, "/api")
      .start
}
