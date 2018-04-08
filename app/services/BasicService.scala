package services

trait BasicService[T] {
  def findAll(): List[T]

  def findById(id: Long): Option[T]

  def update(id: Long, entity: T): Unit

  def insert(entity: T): Unit

  def delete(id: Long): Unit
}
