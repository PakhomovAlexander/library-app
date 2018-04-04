package models.friends


trait FriendService {
  def findAll(): List[Friend]
  def findById(id: Long): Option[Friend]
}
