package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.{ignored, mapping}

case class PublishingHouse(id: Long, name: String)

object PublishingHouse {
  /**
    * Describe the publishing house form (used in both edit and create screens).
    */
  val publishingHouseForm = Form(
    mapping(
      "id" -> ignored[Long](-99),
      "Name" -> nonEmptyText
    )(PublishingHouse.apply)(PublishingHouse.unapply)
  )
}