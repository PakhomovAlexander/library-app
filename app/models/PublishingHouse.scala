package models

case class PublishingHouse(id: Long, name: String)

object PublishingHouse extends Entity {

  override val collectionName: String = "publishingHouses"

}