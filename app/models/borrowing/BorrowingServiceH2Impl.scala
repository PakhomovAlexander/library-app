package models.borrowing
import java.util.Date
import javax.inject.Singleton

import models.books.Book
import models.friends.Friend

@Singleton
class BorrowingServiceH2Impl extends BorrowingService {
  override def borrow(book: Book, friend: Friend, date: Date) = ???

  override def giveBack(book: Book) = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filter: String) = ???
}
