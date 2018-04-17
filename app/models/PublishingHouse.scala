package models

import models.Genre.isName
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, _}

case class PublishingHouse(id: BigInt, name: String)


object PublishingHouse {
  /**
    * Describe the publishing house form (used in both edit and create screens).
    */
  val publishingHouseForm = Form(
    mapping(
      "id" -> ignored[String]("-99"),
      "Name" -> nonEmptyText.verifying("The fio can contain only letters!", name => isName(name))
    )(PublishingHouse.apply)(PublishingHouse.unapplyForm)
  )

  def apply(id: String, name: String): PublishingHouse = new PublishingHouse(BigInt(id), name)

  def unapplyForm(arg: PublishingHouse): Option[(String, String)] = Option(arg.id.toString(), arg.name)

  private def isName(fio: String) = fio.matches("""^([a-zA-Z\s])+$""")
}