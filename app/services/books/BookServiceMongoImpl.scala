package services.books

import java.time.{LocalDate, ZoneId}
import java.util.Date

import models.Book
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import services.MongoHelper._
import services.Page
import services.genres.GenreServiceH2Impl
import services.publishingHouses.PublishingHouseService


class BookServiceMongoImpl(publishingHouseService: PublishingHouseService,
                           genreService: GenreServiceH2Impl) extends BookService {

  case class BookMongo(id: Long,
                       name: String,
                       author: String,
                       pub_year: Option[LocalDate],
                       pub_author: Option[String],
                       translator: Option[String],
                       comment: Option[String],
                       pub_house_id: Option[Long],
                       genres: List[Long])


  private val codecRegistry = fromRegistries(fromProviders(classOf[BookMongo]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[BookMongo] = database.getCollection("books")


  /**
    * Return a list of all books.
    *
    */
  override def findAll(): List[Book] = {

    collection.find().results().toList.map(b => Book.apply(
      b.id,
      b.name,
      b.author,
      Option(Date.from(b.pub_year.get.atStartOfDay(ZoneId.systemDefault()).toInstant)),
      b.pub_author,
      b.translator,
      b.comment,
      b.pub_house_id,
      b.genres))

  }

  /**
    * Return a book.
    *
    * @param id The book id
    */
  override def findById(id: Long): Option[Book] = {

    val opt = Option(collection.find(equal("_id", id)).first().headResult())
    if (opt.isEmpty)
      Option.empty[Book]
    else
      Option(Book.apply(
        opt.get.id,
        opt.get.name,
        opt.get.author,
        Option(Date.from(opt.get.pub_year.get.atStartOfDay(ZoneId.systemDefault()).toInstant)),
        opt.get.pub_author,
        opt.get.translator,
        opt.get.comment,
        opt.get.pub_house_id,
        opt.get.genres))

  }

  /**
    * Return a page of Books.
    *
    * @param page     Page to display
    * @param pageSize Number of friend per page
    * @param orderBy  Book property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Book] = {
    val offset = pageSize * page

    val booksMongo = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val books = booksMongo.map(b => Book.apply(
      b.id,
      b.name,
      b.author,
      Option(Date.from(b.pub_year.get.atStartOfDay(ZoneId.systemDefault()).toInstant)),
      b.pub_author,
      b.translator,
      b.comment,
      b.pub_house_id,
      b.genres))

    val totalRows = collection.count().headResult()

    Page(books, page, offset, totalRows)

  }

  /**
    * Update a book.
    *
    * @param id   The book id
    * @param book The book value.
    */
  override def update(id: Long, book: Book): Unit = {

    collection.replaceOne(equal("_id", id), BookMongo(book.id,
      book.name,
      book.author,
      book.pub_year,
      book.pub_author,
      book.translator,
      book.comment,
      Option(book.pub_house.get.id),
      book.genres.map(g => g.id)
    ))

    //syncGenres(book)
  }

  /**
    * Insert a new Book.
    *
    * @param book The book value.
    */
  override def insert(book: Book): Unit = {

    collection.insertOne(BookMongo(book.id,
      book.name,
      book.author,
      book.pub_year,
      book.pub_author,
      book.translator,
      book.comment,
      Option(book.pub_house.get.id),
      book.genres.map(g => g.id)
    )).results()


     // syncGenres(book)

  }

  /**
    * Delete a book.
    *
    * @param id Id of the book to delete.
    */
  override def delete(id: Long): Unit = {

    collection.deleteOne(equal("_id", id)).results()

  }


}
