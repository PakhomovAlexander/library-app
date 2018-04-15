package services.borrowing

import java.time.LocalDate
import java.util.Date

import models.Borrowing
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters.{equal, _}
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import services.MongoHelper._
import services.Page

class BorrowingServiceMongoImpl extends BorrowingService {

  case class BorrowingMongo(id_book: Long,
                            id_friend: Long,
                            borrow_date: Date,
                            is_lost: Option[Boolean],
                            is_damaged: Option[Boolean],
                            return_date: Option[Date],
                            comment: Option[String])

  private val codecRegistry = fromRegistries(fromProviders(classOf[BorrowingMongo]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[BorrowingMongo] = database.getCollection("borrowing")

  /**
    * Return a page of Borrowing.
    *
    * @param page     Page to display
    * @param pageSize Number of borrows per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Borrowing] = {
    val offset = pageSize * page

    val borrowingMongo = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val borrowing = borrowingMongo.map(
      b => Borrowing.apply( b.id_book,
                            b.id_friend,
                            b.borrow_date,
                            b.is_lost,
                            b.is_damaged,
                            b.return_date,
                            b.comment))

    val totalRows = collection.count().headResult()

    Page(borrowing, page, offset, totalRows)

  }

  /**
    * Return a list of all borrow actions.
    *
    */
  override def findAll(): List[Borrowing] = {
    collection.find().results().toList.map(
      b => Borrowing.apply( b.id_book,
        b.id_friend,
        b.borrow_date,
        b.is_lost,
        b.is_damaged,
        b.return_date,
        b.comment))
  }

  /**
    * Return a list of borrowings filtered by some field
    * @param filterBy The field to be filtered
    * @param filter The filter condition
    * @return
    */
  override def findBy(filterBy: String, filter: String): List[Borrowing] = {

    collection.find(equal(filterBy, filter)).results().toList.map(
      b => Borrowing.apply( b.id_book,
        b.id_friend,
        b.borrow_date,
        b.is_lost,
        b.is_damaged,
        b.return_date,
        b.comment))

  }

  /**
    * Return a list of borrowings find by primary key
    * @param id_friend part of pk
    * @param id_book part of pk
    * @param date part of pk
    * @return
    */
  override def findByPk(id_friend: Long, id_book: Long, date: LocalDate): Option[Borrowing] = {
    val opt = Option(collection.find(and(equal("id_friend", id_friend),
                        equal("id_book", id_book),
                        equal("borrow_date", date)))
      .first().headResult())

    if (opt.isEmpty)
      Option.empty
    else
      Option(Borrowing.apply( opt.get.id_book,
        opt.get.id_friend,
        opt.get.borrow_date,
        opt.get.is_lost,
        opt.get.is_damaged,
        opt.get.return_date,
        opt.get.comment))
  }

  /**
    * Update a borrowing.
    *
    * @param borrowing      The borrowing values.
    */
  override def update(borrowing: Borrowing): Unit = {
    collection.replaceOne(and(
      equal("id_book", borrowing.book.id),
      equal("id_friend", borrowing.friend.id),
      equal("borrow_date", borrowing.borrow_date)),
      BorrowingMongo( borrowing.book.id,
                      borrowing.friend.id.get,
                      borrowing.borrow_date,
                      borrowing.is_lost,
                      borrowing.is_damaged,
                      borrowing.return_date,
                      borrowing.comment))

  }

  /**
    * Insert a new Publishing house.
    *
    * @param borrowing The borrow values.
    */
  override def borrow(borrowing: Borrowing): Unit = {

    val borrowingMongo = BorrowingMongo(
      borrowing.book.id,
      borrowing.friend.id.get,
      borrowing.borrow_date,
      borrowing.is_lost,
      borrowing.is_damaged,
      borrowing.return_date,
      borrowing.comment)

    collection.insertOne(borrowingMongo).results()
  }

  /**
    * Delete a borrowing.
    *
    * @param borrowing borrowing value to delete.
    */
  override def giveBack(borrowing: Borrowing): Unit = {

    collection.deleteOne(and(
      equal("id_book", borrowing.book.id),
      equal("id_friend", borrowing.friend.id),
      equal("borrow_date", borrowing.borrow_date))).results()
  }
}
