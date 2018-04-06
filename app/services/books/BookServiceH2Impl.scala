package services.books

import java.util.Date
import javax.inject.Singleton

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import models._
import play.api.db.DBApi


@Singleton
class BookServiceH2Impl(dbapi: DBApi) extends BookService {
  private val db = dbapi.database("default")

  /**
    * Parse a Book from a ResultSet
    */
  private val simple = {
    get[Option[Long]]("book.id") ~
      get[String]("book.name") ~
      get[String]("book.author") ~
      get[Option[Date]]("book.pub_year") ~
      get[Option[String]]("book.pub_author") ~
      get[Option[String]]("book.translator") ~
      get[Option[String]]("book.comment") ~
      get[Option[PublishingHouse]]("book.pub_house") ~
      get[Option[Genre]]("book.genre") map {
      case id ~ name ~ author ~ pub_year ~ pub_author ~ translator ~ comment ~ pub_house ~ genre =>
        Book(id.get, name, author, pub_year, pub_author, translator, comment, pub_house, genre)
    }
  }

  /**
    * Return a list of all books.
    *
    */
  override def findAll(): List[Book] = db.withConnection { implicit connection =>
    SQL("select * from book order by name").as(simple *)
  }

  /**
    * Return a book.
    *
    * @param id The book id
    */
  override def findById(id: Long): Option[Book] = db.withConnection { implicit connection =>
    SQL("select * from book where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)
  }

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

      Page(books, page, offset, totalRows)
    }
  }

  /**
    * Update a friend.
    *
    * @param id     The friend id
    * @param book The friend values.
    */
  override def update(id: Long, book: Book): Unit = {
//    db.withConnection { implicit connection =>
//      SQL(
//        """
//          update book
//          set name = {name}, author = {author}, pub_year = {pub_year}, pub_author = {pub_author},
//          translator = {translator}, comment = {comment}, pub_house = {pub_house}, genre = {genre}
//          where id = {id}
//        """
//      ).on(
//        'name -> book.name,
//        'author -> book.author,
//        'pub_year -> book.pub_year,
//        'pub_author -> book.pub_author,
//        'translator -> book.translator,
//        'comment -> book.comment,
//        'pub_house -> book.pub_house,
//        'genre -> book.genre,
//        'id -> id
//      ).executeUpdate()
//    }
  }

  /**
    * Insert a new Friend.
    *
    * @param friend The friend values.
    */
  override def insert(friend: Book): Unit = {
//    db.withConnection { implicit connection =>
//      SQL(
//        """
//          insert into friend values (
//            (select next value for friend_seq),
//            {fio}, {phone_number}, {social_number}, {email}, {comment}
//          )
//        """
//      ).on(
//        'fio -> friend.fio,
//        'phone_number -> friend.phone_number,
//        'social_number -> friend.social_number,
//        'email -> friend.email,
//        'comment -> friend.comment
//      ).executeUpdate()
//    }
  }

  /**
    * Delete a friend.
    *
    * @param id Id of the friend to delete.
    */
  override def delete(id: Long): Unit = {
    db.withConnection { implicit connection =>
      SQL("delete from book where id = {id}").on('id -> id).executeUpdate()
    }
  }
}
