package services.borrowing

import java.time.LocalDate

import models.Borrowing
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import services.MongoHelper._
import services.Page

class BorrowingServiceMongoImpl extends BorrowingService {

  private val codecRegistry = fromRegistries(fromProviders(classOf[Borrowing]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[Borrowing] = database.getCollection("friends")

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

    val friends = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val totalRows = collection.count().headResult()

    Page(friends, page, offset, totalRows)

  }

  override def findAll(): List[Borrowing] = ???

  override def findBy(filterBy: String, filter: String): List[Borrowing] = ???

  override def findByPk(id_friend: Long, id_book: Long, date: LocalDate): Option[Borrowing] = ???

  override def update(borrowing: Borrowing): Unit = ???

  override def borrow(borrowing: Borrowing): Unit = ???

  override def giveBack(borrowing: Borrowing): Unit = ???
}
