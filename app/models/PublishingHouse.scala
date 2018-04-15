package models

case class PublishingHouse(id: BigInt, name: String)

object PublishingHouse {

  def apply(id: BigInt, name: String): PublishingHouse = new PublishingHouse(id, name)

}