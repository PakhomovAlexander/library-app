package services.borrowing

import java.util.Date

import models.{Book, Borrowing, Friend}
import services.PageService

trait BorrowingService extends PageService[Borrowing]{
  def borrow(book: Book, friend: Friend, date: Date): Option[Borrowing]
  def giveBack(book: Book): Boolean
}
