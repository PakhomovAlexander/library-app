package services.borrowing

import java.time.LocalDate
import java.util.Date

import javax.inject.Inject
import models.Borrowing
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page
import services.books.BookService
import services.friends.FriendService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class BorrowingServiceReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                           implicit val friendService: FriendService,
                                           bookService: BookService) extends BorrowingService
  with MongoController with ReactiveMongoComponents {


  case class MongoBorrowing (book: BSONObjectID,
                             friend: BSONObjectID,
                             borrow_date: Date,
                             is_lost: Option[Boolean],
                             is_damaged: Option[Boolean],
                             return_date: Option[Date],
                             comment: Option[String])

  private def toBorrowing(mongoBorrowing: MongoBorrowing): Borrowing = {
    new Borrowing(
      bookService.findById(BigInt(mongoBorrowing.book.stringify, 16)).get,
      friendService.findById(BigInt(mongoBorrowing.friend.stringify, 16)).get,
      mongoBorrowing.borrow_date,
      mongoBorrowing.is_lost,
      mongoBorrowing.is_damaged,
      mongoBorrowing.return_date,
      mongoBorrowing.comment
    )
  }

  private def toMongoBorrowing(borrowing: Borrowing): MongoBorrowing = {
    var book = new BSONObjectID
    try {
      book = BSONObjectID.parse(borrowing.book.id.bigInteger.toString(16)).get
    }

    var friend = new BSONObjectID
    try {
      friend = BSONObjectID.parse(borrowing.friend.id.get.bigInteger.toString(16)).get
    }

    MongoBorrowing(book,
      friend,
      borrowing.borrow_date,
      borrowing.is_lost,
      borrowing.is_damaged,
      borrowing.return_date,
      borrowing.comment)

  }

  def collection: Future[BSONCollection] = database.map(
    _.collection[BSONCollection]("friends"))
  implicit def personWriter: BSONDocumentWriter[MongoBorrowing] = Macros.writer[MongoBorrowing]
  implicit def personReader: BSONDocumentReader[MongoBorrowing] = Macros.reader[MongoBorrowing]




  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Borrowing] = {
    val offset = pageSize * page

    val selector = BSONDocument(filterBy -> BSONRegex(filter, "i"))

    val future = collection.flatMap(_.find(selector)
      .sort(BSONDocument(orderBy -> 1))
      .skip(offset)
      .cursor[MongoBorrowing]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoBorrowing]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toBorrowing(f))


    val totalRows = Await.result(collection.flatMap(_.find().cursor[MongoBorrowing]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBorrowing]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toBorrowing(f))

    Page(result, page, offset, totalRows.size)
  }

  override def findAll(): List[Borrowing] = {
    val future = collection.flatMap(_.find().cursor[MongoBorrowing]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBorrowing]]()))

    Await.result(future, Duration.Inf).map(f => toBorrowing(f))
  }

  override def findBy(filterBy: String, filter: String): List[Borrowing] = {
    val selector = BSONDocument(filterBy -> BSONRegex(filter, "i"))

    val future = collection.flatMap(_.find(selector).cursor[MongoBorrowing]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBorrowing]]()))

    Await.result(future, Duration.Inf).map(f => toBorrowing(f))
  }

  override def findByPk(id_friend: BigInt, id_book: BigInt, date: LocalDate): Option[Borrowing] = {
    var book = new BSONObjectID
    try {
      book = BSONObjectID.parse(id_book.bigInteger.toString(16)).get
    }

    var friend = new BSONObjectID
    try {
      friend = BSONObjectID.parse(id_friend.bigInteger.toString(16)).get
    }

    val future = collection.flatMap(_.
      find("book" -> book, "friend" -> friend, "borrow_date" -> date)
      .cursor[MongoBorrowing]()
      .collect[List](-1, Cursor.FailOnError[List[MongoBorrowing]]()))

    Option(Await.result(future, Duration.Inf).map(f => toBorrowing(f)).head)
  }

  override def update(borrowing: Borrowing): Unit = {
    val mongoBorrowing = toMongoBorrowing(borrowing)

    val selector = document("book" -> mongoBorrowing.book,
                                      "friend" -> mongoBorrowing.friend,
                                      "borrow_date" -> mongoBorrowing.borrow_date)

    collection.flatMap(_.update(selector, mongoBorrowing))
  }

  override def borrow(borrowing: Borrowing): Unit = {
    collection.flatMap(_.insert(toMongoBorrowing(borrowing)))
  }

  override def giveBack(borrowing: Borrowing): Unit = {
    val mongoBorrowing = toMongoBorrowing(borrowing)

    val selector = document("book" -> mongoBorrowing.book,
      "friend" -> mongoBorrowing.friend,
      "borrow_date" -> mongoBorrowing.borrow_date)

    collection.flatMap(_.remove(selector))
  }

  }
