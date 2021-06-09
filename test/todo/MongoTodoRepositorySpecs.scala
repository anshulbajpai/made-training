package todo

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting

import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoTodoRepositorySpecs
    extends WordSpec
    with GuiceOneAppPerSuite
    with Injecting
    with ScalaFutures
    with MustMatchers
    with BeforeAndAfterEach {

  private val repo = inject[MongoTodoRepository]

  override protected def beforeEach(): Unit = {
    repo.removeAll().futureValue
  }

  "list" must {
    "return no todos if repo is empty" in {
      repo.list().futureValue mustBe List.empty
    }

    "return todos sorted by created date" in {
      val todo1 =
        Todo(randomUUID().toString, "one", "oneD")
      val todo2 = Todo(randomUUID().toString, "two", "twoD")
      val todo3 =
        Todo(randomUUID().toString, "three", "threeD")

      repo.bulkInsert(List(todo1, todo2, todo3)).futureValue

      repo.list().futureValue mustBe List(todo1, todo2, todo3)
    }
  }

  "create" must {
    "add new todos to the list" in {
      val id = repo.create("one", "oneD").futureValue
      repo.findById(id).futureValue mustBe Some(Todo(id, "one", "oneD"))
    }
  }


  "update" must {
    "return false if the id doesn't exist" in {
      val result: Future[Boolean] = repo.update(
        Todo(randomUUID().toString, "some title", "some description")
      )
      val wasUpdated: Boolean = result.futureValue
      wasUpdated mustBe false
    }

    "return true and update the todo if the id matches" in {
      val todoId = randomUUID().toString
      repo.insert(Todo(todoId, "old title", "old description")).futureValue

      val expectedTodo = Todo(todoId, "some title", "some description")
      val updateResult: Future[Boolean] = repo.update(expectedTodo)
      val wasUpdated: Boolean = updateResult.futureValue
      wasUpdated mustBe true

      val mayBeTodo = repo.findById(todoId).futureValue
      mayBeTodo mustBe Some(expectedTodo)
    }
  }

  "get" must {
    "return the todo if the id exist" in {
      val todo =
        Todo(randomUUID().toString, "title", "descr")
      repo.insert(todo).futureValue
      repo.get(todo.id).futureValue mustBe Some(todo)
    }

    "return None if the todo with id doesn't exist" in {
      repo.get(randomUUID().toString).futureValue mustBe None
    }
  }

  "delete" must {
    "return false if id doesn't exist" in {
      repo
        .delete(randomUUID().toString)
        .futureValue mustBe false
    }

    "return true if id exist and remove the todo" in {
      val todo =
        Todo(randomUUID().toString, "title", "descr")
      repo.insert(todo).futureValue
      repo.delete(todo.id).futureValue mustBe true
      repo.findById(todo.id).futureValue mustBe None
    }
  }
}
