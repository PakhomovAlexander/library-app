package services.books

import java.text.SimpleDateFormat
import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import models._
import play.api.db.DBApi
import services.Page
import services.genres.{GenreService, GenreServiceH2Impl}
import services.publishingHouses.PublishingHouseService
import java.time.format.{DateTimeFormatter, DateTimeParseException}


@Singleton
class BookServiceH2Impl @Inject()(dbapi: DBApi,
                                  publishingHouseService: PublishingHouseService,
                                  genreService: GenreServiceH2Impl)
  extends BookService {

  private val db = dbapi.database("default")


  private val localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.s")
  private val localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  /**
    * Parse a Book from a ResultSet
    */
  private val simple = {
    get[Option[Long]]("book.id") ~
      get[String]("book.name") ~
      get[String]("book.author") ~
      get[Option[String]]("book.pub_year") ~
      get[Option[String]]("book.pic_author") ~
      get[Option[String]]("book.translator") ~
      get[Option[String]]("book.comment") ~
      get[Option[Long]]("book.id_pub_house") map {
      case id ~ name ~ author ~ pub_year ~ pic_author ~ translator ~ comment ~ id_pub_house =>
        new Book(
          id.get,
          name,
          author,
          pub_year.fold(Option.empty[LocalDate])(pub_year =>
            try {
              Option(LocalDate.parse(pub_year, localDateTimeFormatter))
            } catch {
              case e: DateTimeParseException => Option(LocalDate.parse(pub_year, localDateFormatter))
            }),
          pic_author,
          translator,
          comment,
          pubHouse(id_pub_house.getOrElse(-99)),
          Nil
        )
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
  override def findById(id: BigInt): Option[Book] = db.withConnection { implicit connection =>
    val book = SQL("select * from book where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)

    if (book.isDefined) {
      println(s">>>> ${bookWithGenres(book.get)}")
      Option(bookWithGenres(book.get))
    } else {
      Option.empty[Book]
    }
  }

  private def bookWithGenres(book: Book) = {
    book.copy(genres = genreService.genres(book.id))
  }

  /**
    * Return a page of Books.
    *
    * @param page     Page to display
    * @param pageSize Number of friend per page
    * @param orderBy  Book property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int, filterBy: String = "name", filter: String = "%"): Page[Book] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val books = SQL(
        """
          select * from book
          where """ + filterBy +
          """ like {filter}
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
    val gnrs = books.map { book => bookGenres(book) }

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
  override def update(id: BigInt, book: Book): Unit = {
    db.withConnection { implicit connection =>
      book.pub_house.fold(
        SQL(
          """
              update book
              set name = {name}, author = {author}, pub_year = {pub_year}, pic_author = {pic_author},
              translator = {translator}, comment = {comment}
              where id = {id}
            """
        ).on(
          'name -> book.name,
          'author -> book.author,
          'pub_year -> book.pub_year.fold("")(year => year.toString),
          'pic_author -> book.pub_author.orNull,
          'translator -> book.translator.orNull,
          'comment -> book.comment.orNull,
          'id -> id
        ).executeUpdate()
      )(house =>
        SQL(
          """
              update book
              set name = {name}, author = {author}, pub_year = {pub_year}, pic_author = {pic_author},
              translator = {translator}, comment = {comment}, pub_house = {pub_house}
              where id = {id}
            """
        ).on(
          'name -> book.name,
          'author -> book.author,
          'pub_year -> book.pub_year.fold("")(year => year.toString),
          'pic_author -> book.pub_author.orNull,
          'translator -> book.translator.orNull,
          'comment -> book.comment.orNull,
          'pub_house -> house.id,
          'id -> id
        ).executeUpdate()
      )

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
      book.pub_house.fold(
        SQL(
          """
              insert into book(id, name, pub_year, pic_author, translator, author, comment) values (
                  (select next value for book_seq), {name}, {pub_year}, {pic_author}, {translator}, {author}, {comment});
            """
        ).on(
          'name -> book.name,
          'pub_year -> book.pub_year.fold("")(year => year.toString),
          'pic_author -> book.pub_author.orNull,
          'translator -> book.translator.orNull,
          'author -> book.author,
          'comment -> book.comment.orNull
        ).executeUpdate()
      )(house =>
        SQL(
          """
              insert into book(id, name, pub_year, pic_author, translator, author, comment, id_pub_house) values (
                  (select next value for book_seq), {name}, {pub_year}, {pic_author}, {translator}, {author}, {comment}, {id_pub_house});
            """
        ).on(
          'name -> book.name,
          'pub_year -> book.pub_year.fold("")(year => year.toString),
          'pic_author -> book.pub_author.orNull,
          'translator -> book.translator.orNull,
          'author -> book.author,
          'comment -> book.comment.orNull,
          'id_pub_house -> house.id
        ).executeUpdate())

      syncGenres(book)
    }
  }

  /**
    * Delete a book.
    *
    * @param id Id of the book to delete.
    */
  override def delete(id: BigInt): Unit = {
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

      deleteGenres(id)
    }
  }

  private def pubHouse(id: Long) = publishingHouseService.findById(id)

  private def deleteGenres(book_id: BigInt) = {
    db.withConnection { implicit connection =>
      SQL("delete from book_genre where id_book = {id_book}")
        .on('id_book -> book_id)
        .executeUpdate()
    }
  }

  private def bookGenres(book: Book) = {
    genreService.genres(book.id)
  }
}
