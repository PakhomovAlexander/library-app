package services.genres

import javax.inject.Inject
import models.Genre
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


class GenreServiceReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends GenreService
  with MongoController with ReactiveMongoComponents {

  case class MongoGenre(_id: BSONObjectID,
                        name: String,
                        parent_genre: Option[BSONObjectID])

  private def toGenre(mongoGenre: MongoGenre): Genre = {
    if (mongoGenre.parent_genre.nonEmpty)
      new Genre(BigInt(mongoGenre._id.stringify, 16),
        mongoGenre.name,
        findById(BigInt(mongoGenre._id.stringify, 16)))
    else
      new Genre(BigInt(mongoGenre._id.stringify, 16),
        mongoGenre.name,
        None)
  }

  private def toMongoGenre(genre: Genre): MongoGenre = {
    var _id = new BSONObjectID
    try {
      _id = BSONObjectID.parse(genre.id.bigInteger.toString(16)).get
    }
    catch {
      case _: Throwable => _id = BSONObjectID.generate()
    }

    var _id_parent: Option[BSONObjectID] = None
    try {
      _id_parent = Option(BSONObjectID.parse(genre.id.bigInteger.toString(16)).get)
    }
    catch {
      case _: Throwable => _id_parent = None
    }

    MongoGenre(_id,
      genre.name,
      _id_parent)
  }

  private def toMongoGenre(id: BigInt): MongoGenre = {
    var _id = new BSONObjectID
    try {
      _id = BSONObjectID.parse(id.bigInteger.toString(16)).get
    }
    catch {
      case _: Throwable => _id = BSONObjectID.generate()
    }

    MongoGenre(_id,
      "",
      None)
  }

  def collection: Future[BSONCollection] = database.map(
    _.collection[BSONCollection]("genres"))
  implicit def personWriter: BSONDocumentWriter[MongoGenre] = Macros.writer[MongoGenre]
  implicit def personReader: BSONDocumentReader[MongoGenre] = Macros.reader[MongoGenre]


  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Genre] = {
    val offset = pageSize * page

    val selector = BSONDocument(filterBy -> BSONRegex(filter, "i"))

    val future = collection.flatMap(_.find(selector)
      .sort(BSONDocument(orderBy -> 1))
      .skip(offset)
      .cursor[MongoGenre]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoGenre]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toGenre(f))


    val totalRows = Await.result(collection.flatMap(_.find().cursor[MongoGenre]()
      .collect[List](-1, Cursor.FailOnError[List[MongoGenre]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toGenre(f))

    Page(result, page, offset, totalRows.size)
  }

  override def findAll(): List[Genre] = {
    val future = collection.flatMap(_.find().cursor[MongoGenre]()
      .collect[List](-1, Cursor.FailOnError[List[MongoGenre]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toGenre(f))
  }

  override def findById(id: BigInt): Option[Genre] = {
    val future = collection.flatMap(_.find("_id" -> id).cursor[MongoGenre]()
      .collect[List](-1, Cursor.FailOnError[List[MongoGenre]]())) // .FailOnError - обработчик иключения

    Option(Await.result(future, Duration.Inf).map(f => toGenre(f)).head)
  }

  override def update(id: BigInt, entity: Genre): Unit = {
    val mongoFriend = toMongoGenre(entity)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.update(selector, mongoFriend))
  }

  override def insert(entity: Genre): Unit = {
    collection.flatMap(_.insert(toMongoGenre(entity)))
  }

  override def delete(id: BigInt): Unit = {
    val mongoFriend = toMongoGenre(id)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.remove(selector))
  }
}
