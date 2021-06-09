package todo

import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, OptionValues, WordSpec}
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, header, status}
import play.api.test.{FakeRequest, Helpers}

import java.util.UUID.randomUUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TodoControllerSpec extends WordSpec with MustMatchers with OptionValues with MockitoSugar with BeforeAndAfterEach {

  val mockRepo = mock[MongoTodoRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockRepo)
  }

  "list" must {
    "return no todos if there are no todo yet" in {
      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      when(mockRepo.list()).thenReturn(Future.successful(List.empty))

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

      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      when(mockRepo.list()).thenReturn(Future.successful(List(todo1, todo2)))

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
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      val todoJson = Json.obj(
        "title" -> "first todo",
        "description" -> "first todo description"
      )

      val todoId = randomUUID().toString

      when(mockRepo.create("first todo", "first todo description"))
        .thenReturn(Future.successful(todoId))

      val response: Future[Result] =
        controller.create(FakeRequest().withBody(todoJson))

      status(response) mustBe CREATED

      val locationHeader: String = header(HeaderNames.LOCATION, response).value
      val todoIdRegex = "^/api/todos/(.*)$".r
      val todoIdRegex(todoIdInHeader) = locationHeader
      todoIdInHeader mustBe todoId
    }
  }

  "get" must {
    "return NotFound if there are no todos matching the requested id" in {
      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      val id = randomUUID().toString

      when(mockRepo.get(id)).thenReturn(Future.successful(None))

      val response = controller.get(id)(FakeRequest())

      status(response) mustBe NOT_FOUND
    }

    "return the todo matching the id" in {
      val todoId = randomUUID().toString
      val todo = Todo(todoId, "todo title", "todo description")

      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      when(mockRepo.get(todoId)).thenReturn(Future.successful(Some(todo)))

      val response = controller.get(todoId)(FakeRequest())

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.parse(s"""
          |{"id": "$todoId", "title": "${todo.title}", "description" :"${todo.description}"}
          |""".stripMargin)
    }
  }

  "delete" must {
    "return a Not found if no matching todo" in {
      val todoId = randomUUID().toString
      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      when(mockRepo.delete(todoId)).thenReturn(Future.successful(false))

      val response = controller.delete(todoId)(FakeRequest())

      status(response) mustBe NOT_FOUND
    }

    "delete the matching todo" in {
      val todoId = randomUUID().toString
      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      when(mockRepo.delete(todoId)).thenReturn(Future.successful(true))

      val response = controller.delete(todoId)(FakeRequest())

      status(response) mustBe NO_CONTENT
    }
  }

  "update" must {
    "return a Not found if no matching todo" in {
      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      val updateJson = Json.obj(
        "title" -> "updated title",
        "description" -> "updated description"
      )

      when(mockRepo.update(Todo(randomUUID().toString, "don't care", "don't care"))).thenReturn(Future.successful(false))

      val response = controller.update(randomUUID().toString)(FakeRequest().withBody(updateJson))

      status(response) mustBe NOT_FOUND
    }

    "update the existing todo to new values" in {
      val todoId = randomUUID().toString
      val controller =
        new TodoController(Helpers.stubControllerComponents(), mockRepo)

      val updateJson = Json.obj(
        "title" -> "updated title",
        "description" -> "updated description"
      )
      when(mockRepo.update(Todo(todoId, "updated title", "updated description"))).thenReturn(Future.successful(true))

      val response = controller.update(todoId)(FakeRequest().withBody(updateJson))

      status(response) mustBe NO_CONTENT
    }
  }
}
