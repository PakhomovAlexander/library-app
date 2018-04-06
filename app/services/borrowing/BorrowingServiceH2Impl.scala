package services.borrowing

import java.util.Date
import javax.inject.Singleton

import models.{Book, Friend}

@Singleton
class BorrowingServiceH2Impl extends BorrowingService {
  override def borrow(book: Book, friend: Friend, date: Date) = ???

  override def giveBack(book: Book) = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filter: String) = ???
}
