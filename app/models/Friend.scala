package models

import play.api.data.Form
import play.api.data.Forms._

case class Friend(id: Option[Long],
                  fio: String,
                  phone_number: Option[String],
                  social_number: Option[String],
                  email: Option[String],
                  comment: Option[String])

object Friend {
  /**
    * Describe the friend form (used in both edit and create screens).
    */
  val friendForm = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "FIO" -> nonEmptyText.verifying("The fio can contain only letters!", fio => isFIO(fio)),
      "Phone number" -> optional(text).verifying("Phone number should contains 0-9", num => isNumber(num)),
      "Social networks" -> optional(text),
      "Email" -> optional(email),
      "Comment" -> optional(text)
    )(Friend.apply)(Friend.unapply)
  )

  def apply(id: Long,
            fio: String,
            phone_number: String,
            social_number: String,
            email: String,
            comment: String): Friend = new
      Friend(
        Option(id),
        fio,
        Option(phone_number),
        Option(social_number),
        Option(email),
        Option(comment)
      )

  private def isNumber(num: Option[String]) = num.fold(true)(
    n => n.matches("""^([0-9]|-)+$""")
  )

  private def isFIO(fio: String) = fio.matches("""^([a-zA-Z\s])+$""")
}
