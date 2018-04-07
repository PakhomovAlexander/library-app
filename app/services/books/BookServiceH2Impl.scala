package services.books

import java.util.Date
import javax.inject.{Inject, Singleton}

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import models._
import play.api.db.DBApi
import services.publishingHouses.PublishingHouseService


@Singleton
class BookServiceH2Impl @Inject() (dbapi: DBApi, publishingHouseService: PublishingHouseService)
extends BookService {

  private val db = dbapi.database("default")

  /**
    * Parse a Book from a ResultSet
    */
  private val simple = {
    get[Option[Long]]("book.id") ~
      get[String]("book.name") ~
      get[String]("book.author") ~
      get[Option[Date]]("book.pub_year") ~
      get[Option[String]]("book.pic_author") ~
      get[Option[String]]("book.translator") ~
      get[Option[String]]("book.comment") ~
      get[Option[Long]]("book.pub_house") map {
      case id ~ name ~ author ~ pub_year ~ pic_author ~ translator ~ comment ~ pub_house =>
        Book(id.get, name, author, pub_year, pic_author, translator, comment, pubHouse(pub_house.orNull), Nil)
    }
  }

  /**
    * Return a list of all books.
    *
    */
  override def findAll(): List[Book] = db.withConnection { implicit connection =>
    val books = SQL("select * from book order by name").as(simple *)
    booksWithGenres(books)
  }

  /**
    * Return a book.
    *
    * @param id The book id
    */
  override def findById(id: Long): Option[Book] = db.withConnection { implicit connection =>
    val book = SQL("select * from book where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)

    if (book.isDefined) {
      Option(bookWithGenres(book.get))
    } else {
      Option.empty[Book]
    }
  }

  private def bookWithGenres(book: Book) = {
    book.copy(genres = genres(book.id))
  }

  private def genres(id: Long): List[Genre] = {
    Nil
  }

  private def pubHouse(id: Long) = publishingHouseService.findById(id)

  /**
    * Return a page of Books.
    *
    * @param page     Page to display
    * @param pageSize Number of friends per page
    * @param orderBy  Book property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filterBy: String = "name", filter: String = "%"): Page[Book] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val books = SQL(
        """
          select * from book
          where {filterBy} like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offset,
        'filterBy -> filterBy,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(simple *)

      val totalRows = SQL(
        """
          select count(*) from book
          where {filterBy} like {filter}
        """
      ).on(
        'filterBy -> filterBy,
        'filter -> filter
      ).as(scalar[Long].single)

      Page(booksWithGenres(books), page, offset, totalRows)
    }
  }

  private def booksWithGenres(books: List[Book]) = {
    val gnrs = books.map { book => genres(book.id) }
    books.zip(gnrs)
      .map {
        case (book, g: List[Genre]) =>
          book.copy(genres = g)
      }
  }

  /**
    * Update a book.
    *
    * @param id   The book id
    * @param book The book value.
    */
  override def update(id: Long, book: Book): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
              update book
              set name = {name}, author = {author}, pub_year = {pub_year}, pub_author = {pub_author},
              translator = {translator}, comment = {comment}, pub_house = {pub_house}
              where id = {id}
            """
      ).on(
        'name -> book.name,
        'author -> book.author,
        'pub_year -> book.pub_year.orNull,
        'pub_author -> book.pub_author.orNull,
        'translator -> book.translator.orNull,
        'comment -> book.comment.orNull,
        'pub_house -> book.pub_house.orNull.id,
        'id -> id
      ).executeUpdate()

      syncGenres(book)
    }
  }

  private def syncGenres(book: Book)(implicit connection: java.sql.Connection) = {
    SQL(
      """
        delete book_genre
        where id_book = {id_book}
      """
    ).on(
      'id_book -> book.id
    ).executeUpdate()

    val sql = SQL("insert into book_genre (id_book, id_genre) values({id_book}, {id_genre})")

    book.genres.foreach(genre =>
      sql.on(
        'id_book -> book.id,
        'id_genre -> genre.id
      ).executeUpdate())
  }

  /**
    * Insert a new Book.
    *
    * @param book The book value.
    */
  override def insert(book: Book): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
              insert into book(id, name, pub_year, pic_autor, translator, author, comment, id_pub_house) values (
                  {id}, {name}, {pub_year}, {pic_autor}, {translator}, {author}, {comment}, {id_pub_house});
            """
      ).on(
        'id -> book.id,
        'name -> book.name,
        'pub_year -> book.pub_year.orNull,
        'pic_autor -> book.pub_author.orNull,
        'translator -> book.translator.orNull,
        'author -> book.author,
        'comment -> book.comment.orNull,
        'id_pub_house -> book.pub_house.orNull.id
      ).executeUpdate()
      syncGenres(book)
    }
  }

  /**
    * Delete a book.
    *
    * @param id Id of the book to delete.
    */
  override def delete(id: Long): Unit = {
    db.withConnection { implicit connection =>
      SQL("delete from book where id = {id}").on('id -> id).executeUpdate()
      SQL(
        """
        delete book_genre
        where id_book = {id_book}
      """
      ).on(
        'id_book -> id
      ).executeUpdate()

    }
  }
}
