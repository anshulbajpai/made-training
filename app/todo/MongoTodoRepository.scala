package todo

import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.FindAndModifyCommand
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats._

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoTodoRepository @Inject() (mongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Todo, String](
      collectionName = "todos",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = MongoTodoRepository.format,
      idFormat = Format(Reads.StringReads, Writes.StringWrites)
    ) {

  def list(): Future[List[Todo]] = findAll()

  def get(id: String): Future[Option[Todo]] = findById(id)

  def delete(id: String): Future[Boolean] = removeById(id).map(_.n == 1)

  def create(title: String, description: String): Future[String] = {
    val id = UUID.randomUUID().toString
    insert(Todo(id, title, description)).map(_ => id)
  }

  def update(toBeModified: Todo): Future[Boolean] = {

    val updateJson= Json.obj(
      "$set" -> Json.obj(
        "title" -> toBeModified.title,
        "description" -> toBeModified.description
      )
    )

    val resultFuture: Future[FindAndModifyCommand.Result[collection.pack.type]] = findAndUpdate(query = Json.obj("_id" -> toBeModified.id), update = updateJson)

    val result: Future[Boolean] = resultFuture.map { commandResult: FindAndModifyCommand.Result[collection.pack.type] =>
      commandResult.result[Todo] match {
        case Some(_) => true
        case None => false
      }
    }
    result
  }

}

object MongoTodoRepository {
  val format: Format[Todo] = mongoEntity {
    Json.format[Todo]
  }
}
