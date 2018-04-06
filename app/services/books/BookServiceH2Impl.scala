package services.books

import javax.inject.Singleton

import models.{Book, Page}

@Singleton
class BookServiceH2Impl extends BookService {
  override def findAll(): List[Book] = ???

  override def findById(id: Long): Option[Book] = ???

  override def update(id: Long, entity: Book): Unit = ???

  override def insert(entity: Book): Unit = ???

  override def delete(id: Long): Unit = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filter: String): Page[Book] = ???
}
