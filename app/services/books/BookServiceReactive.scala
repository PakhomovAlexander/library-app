package services.books

import java.time.ZoneId
import java.util.Date

import javax.inject.Inject
import models.Book
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page
import services.genres.GenreService
import services.publishingHouses.PublishingHouseService

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class BookServiceReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                    implicit val genreService: GenreService,
                                    publishingHouseService: PublishingHouseService) extends BookService
  with MongoController with ReactiveMongoComponents {

  override def list(page: Int, pageSize: Int, orderBy: Int, filterBy: String = "name", filter: String): Page[Book] = {
    val offset = pageSize * page
    val filterReg = filter filter (_ != '%')

    val selector = BSONDocument(filterBy -> BSONRegex(s"(.*)$filterReg(.*)", "i"))

    val future = collection.flatMap(_.find(selector)
      .sort(BSONDocument(mapOrder(orderBy) -> 1))
      .skip(offset)
      .cursor[MongoBook]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoBook]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toBook(f))


    val totalRows = Await.result(collection.flatMap(_.find(BSONDocument()).cursor[MongoBook]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBook]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toBook(f))

    Page(result, page, offset, totalRows.size)
  }

  private def mapOrder(ord: Int) = ord match {
    case 1 => "_id"
    case 2 => "name"
    case 3 => "author"
    case 4 => "pub_year"
    case 5 => "pub_author"
    case 6 => "translator"
    case 7 => "comment"
  }

  override def findAll(): List[Book] = {
    val future = collection.flatMap(_.find(BSONDocument()).cursor[MongoBook]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBook]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toBook(f))
  }

  private def toBook(mongoBook: MongoBook): Book = {
    new Book(BigInt(mongoBook._id.stringify, 16),
      mongoBook.name,
      mongoBook.author,
      Option(mongoBook.pub_year.get.toInstant.atZone(ZoneId.systemDefault()).toLocalDate),
      mongoBook.pub_author,
      mongoBook.translator,
      mongoBook.comment,
      publishingHouseService.findById(BigInt(mongoBook.pub_house.get.stringify, 16)),
      mongoBook.genres.map(genre_id => genreService.findById(BigInt(genre_id.stringify, 16)).get))
  }

  def collection: Future[BSONCollection] = database.map(
    _.collection[BSONCollection]("books"))

  override def findById(id: BigInt): Option[Book] = {
    val search = toMongoBook(id)

    val future = collection.flatMap(_.find(document("_id" -> search._id)).cursor[MongoBook]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBook]]())) // .FailOnError - обработчик иключения

    Option(Await.result(future, Duration.Inf).map(f => toBook(f)).head)
  }

  implicit def personWriter: BSONDocumentWriter[MongoBook] = Macros.writer[MongoBook]

  implicit def personReader: BSONDocumentReader[MongoBook] = Macros.reader[MongoBook]

  override def update(id: BigInt, entity: Book): Unit = {
    val search = toMongoBook(id)
    val mongoBook = toMongoBook(entity).copy(_id = search._id)

    val selector = document("_id" -> search._id)

    collection.flatMap(_.update(selector, mongoBook))
  }

  override def insert(entity: Book): Unit = {
    collection.flatMap(_.insert(toMongoBook(entity)))

  }

  private def toMongoBook(book: Book): MongoBook = {
    var _id = BSONObjectID.generate()
    try {
      _id = BSONObjectID.parse(book.id.bigInteger.toString(16)).get
    }
    var pub_house: Option[BSONObjectID] = None
    try {
      pub_house = Option(BSONObjectID.parse(book.pub_house.get.id.bigInteger.toString(16)).get)
    }
    catch {
      case _: Throwable => pub_house = None
    }

    var genres: ListBuffer[BSONObjectID] = ListBuffer[BSONObjectID]()

    book.genres.foreach(g => {
      try {
        genres.+=(BSONObjectID.parse(g.id.bigInteger.toString(16)).get)
      }

    })

    MongoBook(_id,
      book.name,
      book.author,
      Option(Date.from(book.pub_year.get.atStartOfDay(ZoneId.systemDefault()).toInstant)),
      book.pub_author,
      book.translator,
      book.comment,
      pub_house,
      genres.toList)

  }

  override def delete(id: BigInt): Unit = {
    val mongoFriend = toMongoBook(id)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.remove(selector))
  }

  private def toMongoBook(id: BigInt): MongoBook = {
    var _id = BSONObjectID.generate()
    try {
      _id = BSONObjectID.parse(id.bigInteger.toString(16)).get
    }

    MongoBook(_id,
      "",
      "",
      Option.empty,
      Option.empty,
      Option.empty,
      Option.empty,
      Option.empty,
      List[BSONObjectID]())

  }

  case class MongoBook(_id: BSONObjectID,
                       name: String,
                       author: String,
                       pub_year: Option[Date],
                       pub_author: Option[String],
                       translator: Option[String],
                       comment: Option[String],
                       pub_house: Option[BSONObjectID],
                       genres: List[BSONObjectID])

}
