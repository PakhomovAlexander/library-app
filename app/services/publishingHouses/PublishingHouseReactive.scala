package services.publishingHouses

import javax.inject.Inject
import models.PublishingHouse
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class PublishingHouseReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends PublishingHouseService
  with MongoController with ReactiveMongoComponents {

  case class MongoPublishingHouse(_id: BSONObjectID,
                                  name: String)

  private def toPublishingHouse(mongoPublishingHouse: MongoPublishingHouse):PublishingHouse = {
    new PublishingHouse(
      BigInt(mongoPublishingHouse._id.stringify, 16),
      mongoPublishingHouse.name)
  }

  private def toMongoPublishingHouse(publishingHouse: PublishingHouse): MongoPublishingHouse = {
    var _id = BSONObjectID.generate()
    try {
      _id = BSONObjectID.parse(publishingHouse.id.bigInteger.toString(16)).get
    }
    catch {
      case _: Throwable => _id = BSONObjectID.generate()
    }

    MongoPublishingHouse(_id,
      publishingHouse.name)

  }

  private def toMongoPublishingHouse(id: BigInt): MongoPublishingHouse = {
    var _id = BSONObjectID.generate()
    try {
      _id = BSONObjectID.parse(id.bigInteger.toString(16)).get
    }
    catch {
      case _: Throwable => _id = BSONObjectID.generate()
    }

    MongoPublishingHouse(_id, "")

  }

  private def mapOrder(ord: Int) = ord match {
    case 1 => "_id"
    case 2 => "name"
  }


  def collection: Future[BSONCollection] = database.map(
    _.collection[BSONCollection]("publishinghouses"))
  implicit def personWriter: BSONDocumentWriter[MongoPublishingHouse] = Macros.writer[MongoPublishingHouse]
  implicit def personReader: BSONDocumentReader[MongoPublishingHouse] = Macros.reader[MongoPublishingHouse]


  override def list(page: Int, pageSize: Int, orderBy: Int, filterBy: String, filter: String): Page[PublishingHouse] ={
    val offset = pageSize * page

    val selector = BSONDocument(filterBy -> BSONRegex(filter, "i"))

    val future = collection.flatMap(_.find(selector)
      .sort(BSONDocument(mapOrder(orderBy) -> 1))
      .skip(offset)
      .cursor[MongoPublishingHouse]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoPublishingHouse]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toPublishingHouse(f))


    val totalRows = Await.result(collection.flatMap(_.find(BSONDocument()).cursor[MongoPublishingHouse]()
      .collect[List](-1, Cursor.FailOnError[List[MongoPublishingHouse]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toPublishingHouse(f))

    Page(result, page, offset, totalRows.size)

  }

  override def findAll(): List[PublishingHouse] = {
    val future = collection.flatMap(_.find(BSONDocument()).cursor[MongoPublishingHouse]()
      .collect[List](-1, Cursor.FailOnError[List[MongoPublishingHouse]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toPublishingHouse(f))
  }

  override def findById(id: BigInt): Option[PublishingHouse] = {
    val search = toMongoPublishingHouse(id)

    val future = collection.flatMap(_.find(BSONDocument("_id" -> search._id)).cursor[MongoPublishingHouse]()
      .collect[List](-1, Cursor.FailOnError[List[MongoPublishingHouse]]())) // .FailOnError - обработчик иключения

    Option(Await.result(future, Duration.Inf).map(f => toPublishingHouse(f)).head)
  }

  override def update(id: BigInt, entity: PublishingHouse): Unit = {
    val search = toMongoPublishingHouse(id)
    val mongoPublishingHouse = toMongoPublishingHouse(entity)

    val selector = document("_id" -> search._id)

    collection.flatMap(_.update(selector, mongoPublishingHouse))
  }

  override def insert(entity: PublishingHouse): Unit = {
    collection.flatMap(_.insert(toMongoPublishingHouse(entity)))
  }

  override def delete(id: BigInt): Unit = {
    val mongoFriend = toMongoPublishingHouse(id)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.remove(selector))
  }
}
