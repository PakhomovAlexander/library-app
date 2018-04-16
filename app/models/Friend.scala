package models

import play.api.data.Form
import play.api.data.Forms._

case class Friend(id: Option[BigInt],
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
      "id" -> ignored(None: Option[String]),
      "FIO" -> nonEmptyText.verifying("The fio can contain only letters!", fio => isFIO(fio)),
      "Phone number" -> optional(text).verifying("Phone number should contains 0-9", num => isNumber(num)),
      "Social networks" -> optional(text),
      "Email" -> optional(email),
      "Comment" -> optional(text)
    )(Friend.applyForm)(Friend.unapplyForm)
  )

  def applyForm(id: Option[String],
            fio: String,
            phone_number: Option[String],
            social_number: Option[String],
            email: Option[String],
            comment: Option[String]): Friend = new
      Friend(
//        Option(BigInt(id.get)),
        id map ( x => BigInt(x) ),
        fio,
        phone_number,
        social_number,
        email,
        comment
      )

  def unapplyForm(arg: Friend): Option[(Option[String], String, Option[String],
    Option[String], Option[String], Option[String])] =
    Option(arg.id.map(_ toString), arg.fio, arg.phone_number, arg.social_number, arg.email, arg.comment)

  private def isNumber(num: Option[String]) = num.fold(true)(
    n => n.matches("""^([0-9]|-)+$""")
  )

  private def isFIO(fio: String) = fio.matches("""^([a-zA-Z\s])+$""")
}
