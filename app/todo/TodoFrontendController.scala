package todo

import play.api.mvc.MessagesControllerComponents
import todo.html.{ListTodoView, TodoView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TodoFrontendController @Inject()(
  messagesControllerComponents: MessagesControllerComponents,
  todoApiConnector: TodoApiConnector,
  todoView: TodoView,
  listTodoView: ListTodoView
)(implicit ec: ExecutionContext)
    extends FrontendController(messagesControllerComponents) {

  val list = Action.async { implicit request =>
    todoApiConnector.list().map(todos => Ok(listTodoView(todos)))
  }

  def get(id: String) = Action.async {
    todoApiConnector.get(id).map {
      case Some(todo) => Ok(todoView(todo))
      case None       => NotFound
    }
  }

}
