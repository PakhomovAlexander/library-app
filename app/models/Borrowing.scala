package models

import java.sql.{SQLClientInfoException, SQLException}
import java.util.Date

import services.books.BookService
import services.friends.FriendService

case class Borrowing(book: Book,
                     friend: Friend,
                     borrow_date: Date,
                     is_lost: Option[Boolean],
                     is_damaged: Option[Boolean],
                     return_date: Option[Date],
                     comment: Option[String])

object Borrowing extends Entity {

  override val collectionName: String = "borrowings"

  def apply(id_book: Long,
            id_friend: Long,
            borrow_date: Date,
            is_lost: Option[Boolean],
            is_damaged: Option[Boolean],
            return_date: Option[Date],
            comment: Option[String])(
             implicit bookService: BookService,
             friendService: FriendService): Borrowing =
    new Borrowing(
      bookService.findById(id_book).getOrElse(throw new SQLException("no such book")),
      friendService.findById(id_friend).getOrElse(throw new SQLException("no such friend")),
      borrow_date,
      is_lost,
      is_damaged,
      return_date,
      comment
    )

  def unapplyForm(arg: Borrowing): Option[(Long, Long, Date, Option[Boolean],
    Option[Boolean], Option[Date], Option[String])] =
    Option((
      arg.book.id,
      arg.friend.id.get,
      arg.borrow_date,
      arg.is_lost,
      arg.is_damaged,
      arg.return_date,
      arg.comment))
}
