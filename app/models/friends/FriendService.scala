package models.friends

import models.Page


trait FriendService {
  def findAll(): List[Friend]

  def findById(id: Long): Option[Friend]

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[Friend]

  def update(id: Long, friend: Friend)

  def insert(friend: Friend)

  def delete(id: Long)
}
