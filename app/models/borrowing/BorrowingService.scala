package models.borrowing

import java.util.Date

import models.books.Book
import models.friends.Friend

trait BorrowingService {
  def borrow(book: Book, friend: Friend, date: Date): Option[Borrowing]
  def giveBack(book: Book): Boolean
}
