package services.borrowing

import java.time.LocalDate
import java.util.Date
import javax.inject.{Inject, Singleton}

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import models._
import play.api.db.DBApi
import services.Page
import services.books.BookService
import services.friends.FriendService

@Singleton
class BorrowingServiceH2Impl @Inject() (dbapi: DBApi,
                                        bookService: BookService,
                                        friendService: FriendService) extends BorrowingService {
  private val db = dbapi.database("default")

  /**
    * Parse a borrowing from a ResultSet
    */
  private val simple = {
    get[Long]("borrowing.id_book") ~
    get[Long]("borrowing.id_friend") ~
    get[Date]("borrowing.borrow_date") ~
    get[Option[Boolean]]("borrowing.is_lost") ~
    get[Option[Boolean]]("borrowing.is_damaged") ~
    get[Option[Date]]("borrowing.return_date") ~
    get[Option[String]]("borrowing.comment") map {
      case id_book ~ id_friend ~ borrow_date ~ is_lost ~ is_damaged ~ return_date ~ comment =>
        Borrowing(bookService.findById(id_book).get,
          friendService.findById(id_friend).get,
          borrow_date,
          is_lost,
          is_damaged,
          return_date,
          comment)
    }
  }

  /**
    * Return a list of all borrow actions.
    *
    */
  override def findAll(): List[Borrowing] = db.withConnection { implicit connection =>
    SQL("select * from borrow order by borrow_date").as(simple *)
  }

  /**
    * Return a list of borrowings filtered by some field
    * @param filterBy The field to be filtered
    * @param filter The filter condition
    * @return
    */
  override def findBy(filterBy: String = "id_book", filter: String): List[Borrowing] =
    db.withConnection { implicit connection =>
    SQL("select * from borrowing where {filterBy} = {filter}")
      .on('filterBy -> filterBy, 'filter -> filter)
      .as(simple *)
  }
  /**
    * Return a list of borrowings find by primary key
    * @param id_friend part of pk
    * @param id_book part of pk
    * @param date part of pk
    * @return
    */
  override def findByPk(id_friend: BigInt, id_book: BigInt, date: LocalDate): Option[Borrowing] =
    db.withConnection { implicit connection =>
      SQL("select * from borrowing where id_friend = {id_friend} and id_book = {id_book} and borrow_date = {date}")
        .on('id_friend -> id_friend,
          'id_book -> id_book,
          'date -> date.toString
        )
        .as(simple singleOpt)
    }


  /**
    * Return a page of borrowings.
    *
    * @param page     Page to display
    * @param pageSize Number of borrowings per page
    * @param orderBy  Publishing house property used for sorting
    * @param filter   Filter applied on the filterBy column
    * @param filterBy Column to be filtered
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1,
                    filterBy: String = "borrow_date", filter: String = "%"): Page[Borrowing] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val borrowings = SQL(
        """
          select * from borrowing
          where """ + filterBy + """ like {filter}
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
          select count(*) from borrowing
          where {filterBy} like {filter}
        """
      ).on(
        'filterBy -> filterBy,
        'filter -> filter
      ).as(scalar[Long].single)

      Page(borrowings, page, offset, totalRows)
    }
  }

  /**
    * Update a borrowing.
    *
    * @param borrowing      The borrowing values.
    */
  override def update(borrowing: Borrowing): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          update borrowing
          set is_lost = {is_lost}, is_damaged = {is_damaged}, return_date = {return_date}, comment = {comment}
          where id_book = {id_book} and id_friend = {id_friend} and borrow_date = {borrow_date}
        """
      ).on(
        'id_book -> borrowing.book.id,
        'id_friend -> borrowing.friend.id,
        'borrow_date -> borrowing.borrow_date,
        'is_lost -> borrowing.is_lost,
        'is_damaged -> borrowing.is_damaged,
        'return_date -> borrowing.return_date,
        'comment -> borrowing.comment
      ).executeUpdate()
    }
  }

  /**
    * Insert a new Publishing house.
    *
    * @param borrowing The borrow values.
    */
  override def borrow(borrowing: Borrowing): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into borrowing values (
            {id_book},
            {id_friend},
            {borrow_date},
            {is_lost},
            {is_damaged},
            {return_date},
            {comment}
          )
        """
      ).on(
        'id_book -> borrowing.book.id,
        'id_friend -> borrowing.friend.id,
        'borrow_date -> borrowing.borrow_date,
        'is_lost -> borrowing.is_lost,
        'is_damaged -> borrowing.is_damaged,
        'return_date -> borrowing.return_date,
        'comment -> borrowing.comment
      ).executeUpdate()
    }
  }

  /**
    * Delete a borrowing.
    *
    * @param borrowing borrowing value to delete.
    */
  override def giveBack(borrowing: Borrowing): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """delete from borrowing
            where id_book = {id_book} and
             id_friend = {id_friend} and
             borrow_date = {borrow_date}"""
      ).on(
        'id_book -> borrowing.book.id,
        'id_friend -> borrowing.friend.id,
        'borrow_date -> borrowing.borrow_date
      ).executeUpdate()
    }
  }
}
