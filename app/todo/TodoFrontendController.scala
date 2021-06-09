package todo

import play.api.mvc.{BaseController, ControllerComponents}
import todo.html.ListTodoView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TodoFrontendController @Inject()(
  val controllerComponents: ControllerComponents,
  todoApiConnector: TodoApiConnector,
  listTodoView: ListTodoView
)(implicit ec: ExecutionContext) extends BaseController {

  val list = Action.async {
    todoApiConnector.list().map(todos =>
      Ok(listTodoView(todos))
    )
  }

  def get(id: String) = Action {
    Ok
  }

}
