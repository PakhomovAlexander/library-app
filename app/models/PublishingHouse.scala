package models

case class PublishingHouse(id: Long, name: String)

object PublishingHouse {

  def apply(id: Long, name: String): PublishingHouse = new PublishingHouse(id, name)

}