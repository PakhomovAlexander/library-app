package models.publishingHouses
import javax.inject.Singleton

import models.Page

@Singleton
class PublishingHouseServiceH2Impl extends PublishingHouseService {
  override def findAll(): List[PublishingHouse] = ???

  override def findById(id: Long): Option[PublishingHouse] = ???

  override def update(id: Long, entity: PublishingHouse): Unit = ???

  override def insert(entity: PublishingHouse): Unit = ???

  override def delete(id: Long): Unit = ???

  override def list(page: Int, pageSize: Int, orderBy: Int, filter: String): Page[PublishingHouse] = ???
}
