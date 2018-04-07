package services.borrowing

import java.util.Date
import javax.inject.{Inject, Singleton}

import models.{Book, Friend}

@Singleton
class BorrowingServiceH2Impl @Inject()  extends BorrowingService {
  override def borrow(book: Book, friend: Friend, date: Date) = ???

  override def giveBack(book: Book) = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filterBy: String = "id", filter: String) = ???
}
