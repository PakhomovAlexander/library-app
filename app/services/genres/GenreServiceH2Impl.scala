package services.genres

import javax.inject.{Inject, Singleton}

import models.{Genre, Page}

@Singleton
class GenreServiceH2Impl @Inject()  extends GenreService {
  override def findAll(): List[Genre] = ???

  override def findById(id: Long): Option[Genre] = ???

  override def update(id: Long, entity: Genre): Unit = ???

  override def insert(entity: Genre): Unit = ???

  override def delete(id: Long): Unit = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filterBy: String = "id", filter: String): Page[Genre] = ???
}
