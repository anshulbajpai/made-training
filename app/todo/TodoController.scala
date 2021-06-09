package todo

import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Todo(id: String, title: String, description: String)

case class CreateTodoRequest(title: String, description: String)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents, repository: MongoTodoRepository)(implicit ec: ExecutionContext)
    extends BaseController {

  private implicit val todoWrites = Json.writes[Todo]
  private implicit val createTodoReads = Json.reads[CreateTodoRequest]

  val list: Action[AnyContent] = Action.async {
    val result: Future[Result] = repository.list().map { todo =>
      Ok(Json.toJson(todo))
    }
    result
  }

  val create: Action[JsValue] = Action(parse.json).async { request =>
    val createTodoRequest: CreateTodoRequest =
      request.body.as[CreateTodoRequest]

    val idFut: Future[String] = repository.create(createTodoRequest.title, createTodoRequest.description)

    idFut.map { id =>
      Created.withHeaders(
        HeaderNames.LOCATION -> routes.TodoController.get(id).url
      )
    }
  }

  def update(id: String): Action[JsValue] = Action(parse.json).async { request =>
    val createTodoRequest: CreateTodoRequest =
      request.body.as[CreateTodoRequest]

    repository.update(Todo(id, createTodoRequest.title, createTodoRequest.description)).map {
      case true => NoContent
      case false => NotFound
    }
  }

  def get(id: String) = Action.async {
    val maybeTodoFut: Future[Option[Todo]] = repository.get(id)

    maybeTodoFut.map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None => NotFound
    }
  }

  def delete(id: String) = Action.async {
    val maybeDeletedFut: Future[Boolean] = repository.delete(id)

    maybeDeletedFut.map {
      case true => NoContent
      case false => NotFound
    }
  }
}
