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
    "return an Option of None if doesn't exist" in {
      val todoId = randomUUID().toString

      val result = repo.get(todoId).futureValue
      result mustBe None
    }

    "return an Option of Todo if does exist" in {
      val todoId = randomUUID().toString
      val expectedTodo = Todo(todoId, "old title", "old description")
      repo.insert(expectedTodo).futureValue

      val result = repo.get(todoId).futureValue
      result mustBe Some(expectedTodo)
    }

    "returns correct Todo based on ID" in {
      val wantedTodoId = randomUUID().toString
      val expectedTodo = Todo(wantedTodoId, "old title", "old description")
      val notWantedTodo = Todo(randomUUID().toString, "old title", "old description")

      repo.insert(expectedTodo).futureValue
      repo.insert(notWantedTodo).futureValue

      val result = repo.get(wantedTodoId).futureValue

      result mustBe Some(expectedTodo)
      result mustNot be(Some(notWantedTodo))
    }
  }

}
