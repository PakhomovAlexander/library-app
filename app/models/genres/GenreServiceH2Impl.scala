package models.genres

import javax.inject.Singleton

import models.Page

@Singleton
class GenreServiceH2Impl extends GenreService {
  override def findAll(): List[Genre] = ???

  override def findById(id: Long): Option[Genre] = ???

  override def update(id: Long, entity: Genre): Unit = ???

  override def insert(entity: Genre): Unit = ???

  override def delete(id: Long): Unit = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filter: String): Page[Genre] = ???
}
