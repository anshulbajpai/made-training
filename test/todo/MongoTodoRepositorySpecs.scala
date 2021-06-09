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

}
