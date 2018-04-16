package models

import java.sql.SQLException
import java.util.Date

import play.api.data.Form
import play.api.data.Forms.{date, mapping, optional, text}
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
  /**
    * Describe the borrowing form (used in both edit and create screens).
    */
  lazy val borrowingForm: ((BookService, FriendService) => Form[Borrowing]) =
    (bs: BookService, fs: FriendService) => {
      implicit val bsImplct = bs
      implicit val fsImplct = fs
      Form(
        mapping(
          "id_friend" -> text,
          "id_book" -> text,
          "borrowing_date" -> date,
          "is_lost" -> optional(text),
          "is_damaged" -> optional(text),
          "return_date" -> optional(date),
          "comment" -> optional(text)
        )(Borrowing.apply)(Borrowing.unapplyForm)
      )
    }

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
      bookService.findById(BigInt(id_book)).getOrElse(throw new SQLException("no such book")),
      friendService.findById(BigInt(id_friend)).getOrElse(throw new SQLException("no such friend")),
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
      arg.is_lost.map(b => if (b) "Yes" else "No"),
      arg.is_damaged.map(b => if (b) "Yes" else "No"),
      arg.return_date,
      arg.comment))
}
