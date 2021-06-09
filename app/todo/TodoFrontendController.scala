package todo

import play.api.mvc.{BaseController, ControllerComponents}
import todo.html.{ListTodoView, TodoView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TodoFrontendController @Inject()(
  val controllerComponents: ControllerComponents,
  todoApiConnector: TodoApiConnector,
  todoView: TodoView,
  listTodoView: ListTodoView
)(implicit ec: ExecutionContext)
    extends BaseController {

  val list = Action.async {
    todoApiConnector.list().map(todos => Ok(listTodoView(todos)))
  }

  def get(id: String) = Action.async {
    todoApiConnector.get(id).map {
      case Some(todo) => Ok(todoView(todo))
      case None       => NotFound
    }
  }

}
