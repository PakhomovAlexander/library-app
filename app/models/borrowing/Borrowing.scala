package models.borrowing


import java.util.Date

import models.books.Book
import models.friends.Friend

case class Borrowing(book: Book,
                     friend: Friend,
                     borrowDate: Date,
                     isLost: Option[Boolean],
                     isDamaged: Option[Boolean],
                     returnDate: Option[Date],
                     comment: Option[String])
