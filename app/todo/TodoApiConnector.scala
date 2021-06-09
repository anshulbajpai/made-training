package todo

import play.api.Configuration
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TodoApiConnector @Inject()(
  wsClient: WSClient,
  configuration: Configuration
)(implicit ec: ExecutionContext) {

  private implicit val readTodo = Json.reads[Todo]

  private val apiBaseUrl =
    s"${configuration.get[String]("todos-api-baseurl")}/api"

  def list(): Future[List[Todo]] = {
    val responseFut: Future[WSResponse] =
      wsClient.url(s"$apiBaseUrl/todos").get()
    responseFut.map { response =>
      response.json.as[List[Todo]]
    }
  }

  def get(id: String): Future[Option[Todo]] = {
    wsClient.url(s"$apiBaseUrl/todos/$id").get().map { response =>
      if (response.status == NOT_FOUND) None else Some(response.json.as[Todo])
    }
  }

}
