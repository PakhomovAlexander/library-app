package models.borrowing

import java.util.Date

import models.books.Book
import models.friends.Friend
import models.services.PageService

trait BorrowingService extends PageService[Borrowing]{
  def borrow(book: Book, friend: Friend, date: Date): Option[Borrowing]
  def giveBack(book: Book): Boolean
}
