package models.borrowing


import java.util.Date

import models.books.Book
import models.friends.Friend

case class Borrowing(book: Book,
                     friend: Friend,
                     borrow_date: Date,
                     is_lost: Option[Boolean],
                     is_damaged: Option[Boolean],
                     return_date: Option[Date],
                     comment: Option[String])
