package models.friends

case class Friend(id: Option[Long],
                  fio: String,
                  phoneNumber: Option[String],
                  socialNumber: Option[String],
                  email: Option[String],
                  comment: Option[String])

object Friend {
  def apply(id: Long,
            fio: String,
            phoneNumber: String,
            socialNumber: String,
            email: String,
            comment: String): Friend = new
      Friend(
        Option(id),
        fio,
        Option(phoneNumber),
        Option(socialNumber),
        Option(email),
        Option(comment)
      )
}
