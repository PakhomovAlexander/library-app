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

object Borrowing {
  def apply(id_book: String,
            id_friend: String,
            borrow_date: Date,
            is_lost: Option[String],
            is_damaged: Option[String],
            return_date: Option[Date],
            comment: Option[String])(
             implicit bookService: BookService,
             friendService: FriendService): Borrowing =
    new Borrowing(
      bookService.findById(id_book.toLong).getOrElse(throw new SQLException("no such book")),
      friendService.findById(id_friend.toLong).getOrElse(throw new SQLException("no such friend")),
      borrow_date,
      Option(is_lost.get == "Yes"),
      Option(is_damaged.get == "Yes"),
      return_date,
      comment
    )


  def unapplyForm(arg: Borrowing): Option[(String, String, Date, Option[String],
    Option[String], Option[Date], Option[String])] =
    Option((
      arg.book.id.toString,
      arg.friend.id.get.toString,
      arg.borrow_date,
      if (arg.is_lost.get) Option("Yes") else Option("No"),
      if (arg.is_damaged.get) Option("Yes") else Option("No"),
      arg.return_date,
      arg.comment))
}
