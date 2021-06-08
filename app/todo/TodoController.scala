package todo

import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{
  Action,
  AnyContent,
  BaseController,
  ControllerComponents,
  Result
}

import java.util.UUID.randomUUID
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer

case class Todo(id: String, title: String, description: String)

case class CreateTodoRequest(title: String, description: String)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents,
                               todos: ListBuffer[Todo])
    extends BaseController {

  private implicit val todoWrites = Json.writes[Todo]
  private implicit val createTodoReads = Json.reads[CreateTodoRequest]

  val list: Action[AnyContent] = Action {
    Ok(Json.toJson(todos))
  }

  val create: Action[JsValue] = Action(parse.json) { request =>
    val createTodoRequest: CreateTodoRequest =
      request.body.as[CreateTodoRequest]
    val todo = Todo(
      id = randomUUID().toString,
      title = createTodoRequest.title,
      description = createTodoRequest.description
    )
    todos += todo
    Created.withHeaders(HeaderNames.LOCATION -> routes.TodoController.get(todo.id).url)
  }

  def get(id: String) = Action {
    val maybeTodo = todos.find(_.id == id)
    maybeTodo.fold(NotFound: Result)(todo => Ok(Json.toJson(todo)))
  }

}
