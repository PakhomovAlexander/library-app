package models.books

trait BookService {
  def findAll(): List[Book]
  def findById(id: Long): Option[Book]
}
