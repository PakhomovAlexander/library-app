package services.genres

import javax.inject.Inject
import models.Genre
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class GenreServiceReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends GenreService
  with MongoController with ReactiveMongoComponents {

  override def list(page: Int, pageSize: Int, orderBy: Int, filterBy: String, filter: String): Page[Genre] = {
    val offset = pageSize * page

    val selector = BSONDocument(filterBy -> BSONRegex(filter, "i"))

    val future = collection.flatMap(_.find(selector)
      .sort(BSONDocument(mapOrder(orderBy) -> 1))
      .skip(offset)
      .cursor[MongoGenre]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoGenre]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toGenre(f))


    val totalRows = Await.result(collection.flatMap(_.find(BSONDocument()).cursor[MongoGenre]()
      .collect[List](-1, Cursor.FailOnError[List[MongoGenre]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toGenre(f))

    Page(result, page, offset, totalRows.size)
  }

  override def findAll(): List[Genre] = {
    val future = collection.flatMap(_.find(BSONDocument()).cursor[MongoGenre]()
      .collect[List](-1, Cursor.FailOnError[List[MongoGenre]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toGenre(f))
  }

  override def findById(id: BigInt): Option[Genre] = {
    val search = toMongoGenre(id)

    val future = collection.flatMap(_.find(BSONDocument("_id" -> search._id)).cursor[MongoGenre]()
      .collect[List](-1, Cursor.FailOnError[List[MongoGenre]]())) // .FailOnError - обработчик иключения

    Option(Await.result(future, Duration.Inf).map(f => toGenre(f)).head)
  }

  override def update(id: BigInt, entity: Genre): Unit = {

    val search = toMongoGenre(id)
    val mongoGenres = toMongoGenre(entity)

    val selector = document("_id" -> search._id)

    collection.flatMap(_.update(selector, mongoGenres))
  }

  private def toMongoGenre(genre: Genre): MongoGenre = {
    var _id = BSONObjectID.generate()
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

  override def insert(entity: Genre): Unit = {
    collection.flatMap(_.insert(toMongoGenre(entity)))
  }

  implicit def personWriter: BSONDocumentWriter[MongoGenre] = Macros.writer[MongoGenre]

  implicit def personReader: BSONDocumentReader[MongoGenre] = Macros.reader[MongoGenre]

  override def delete(id: BigInt): Unit = {
    val mongoFriend = toMongoGenre(id)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.remove(selector))
  }

  private def toMongoGenre(id: BigInt): MongoGenre = {
    var _id = BSONObjectID.generate()
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

  private def mapOrder(ord: Int) = ord match {
    case 1 => "_id"
    case 2 => "name"
    case 3 => "parent_genre"
  }

  case class MongoGenre(_id: BSONObjectID,
                        name: String,
                        parent_genre: Option[BSONObjectID])
}
