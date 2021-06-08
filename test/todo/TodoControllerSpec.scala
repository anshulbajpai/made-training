package todo

import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, header, status}
import play.api.test.{FakeRequest, Helpers}

import java.util.UUID.randomUUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class TodoControllerSpec extends WordSpec with MustMatchers with OptionValues {

  "list" must {
    "return no todos if there are no todo yet" in {
      val controller =
        new TodoController(Helpers.stubControllerComponents(), ListBuffer.empty)
      val response: Future[Result] = controller.list(FakeRequest())

      status(response) mustBe OK
      contentAsString(response) mustBe "[]"
    }

    "return todos if there are some todos" in {
      val todo1 = Todo(
        id = randomUUID().toString,
        title = "first todo",
        description = "first todo description"
      )
      val todo2 = Todo(
        id = randomUUID().toString,
        title = "second todo",
        description = "second todo description"
      )
      val todos = ListBuffer(todo1, todo2)
      val controller =
        new TodoController(Helpers.stubControllerComponents(), todos)

      val response: Future[Result] = controller.list(FakeRequest())

      status(response) mustBe OK
      val json = contentAsJson(response)
      json mustBe
        Json.parse(s"""
          |[
          | {"id":"${todo1.id}", "title": "first todo", "description": "first todo description"},
          | {"id":"${todo2.id}", "title": "second todo", "description": "second todo description"}
          |]
          |""".stripMargin)
    }
  }

  "create" must {
    "create a new todo and return its location" in {
      val todos = ListBuffer.empty[Todo]
      val controller =
        new TodoController(Helpers.stubControllerComponents(), todos)

      val todoJson = Json.obj(
        "title" -> "first todo",
        "description" -> "first todo description"
      )

      val response: Future[Result] =
        controller.create(FakeRequest().withBody(todoJson))

      status(response) mustBe CREATED

      val locationHeader: String = header(HeaderNames.LOCATION, response).value
      val todoIdRegex = "^/api/todos/(.*)$".r
      val todoIdRegex(todoId) = locationHeader
      todos mustBe ListBuffer(
        Todo(
          id = todoId,
          title = "first todo",
          description = "first todo description"
        )
      )
    }
  }

  "get" must {
    "return NotFound if there are no todos matching the requested id" in {
      val controller =
        new TodoController(Helpers.stubControllerComponents(), ListBuffer.empty)
      val id = randomUUID().toString
      val response = controller.get(id)(FakeRequest())

      status(response) mustBe NOT_FOUND
    }

    "return the todo matching the id" in {
      val todoId = randomUUID().toString
      val todo = Todo(todoId, "todo title", "todo description")

      val controller =
        new TodoController(Helpers.stubControllerComponents(), ListBuffer(todo))

      val response = controller.get(todoId)(FakeRequest())

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.parse(s"""
          |{"id": "$todoId", "title": "${todo.title}", "description" :"${todo.description}"}
          |""".stripMargin)
    }
  }
}
