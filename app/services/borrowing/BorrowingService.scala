package services.borrowing

import java.time.LocalDate

import models.Borrowing
import services.PageService

trait BorrowingService extends PageService[Borrowing] {
  def findAll(): List[Borrowing]

  def findBy(filterBy: String = "id_book", filter: String): List[Borrowing]

  def findByPk(id_friend: Long, id_book: Long, date: LocalDate): Option[Borrowing]

  def update(borrowing: Borrowing): Unit

  def borrow(borrowing: Borrowing): Unit

  def giveBack(borrowing: Borrowing): Unit
}
