package services

trait BasicService[T] {
  def findAll(): List[T]

  def findById(id: BigInt): Option[T]

  def update(id: BigInt, entity: T): Unit

  def insert(entity: T): Unit

  def delete(id: BigInt): Unit
}
