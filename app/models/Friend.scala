package models

case class Friend(id: Option[BigInt],
                  fio: String,
                  phone_number: Option[String],
                  social_number: Option[String],
                  email: Option[String],
                  comment: Option[String])

object Friend {

  def apply(id: BigInt,
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
}
