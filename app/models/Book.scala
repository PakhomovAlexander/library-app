package models

import java.time.{LocalDate, ZoneId}
import java.util.Date

import play.api.data.Form
import play.api.data.Forms._
import services.genres.GenreService
import services.publishingHouses.PublishingHouseService

case class Book(id: BigInt,
                name: String,
                author: String,
                pub_year: Option[LocalDate],
                pub_author: Option[String],
                translator: Option[String],
                comment: Option[String],
                pub_house: Option[PublishingHouse],
                genres: List[Genre])

object Book {
  /**
    * Describe the book form (used in both edit and create screens).
    */
  lazy val bookForm: ((PublishingHouseService, GenreService) => Form[Book]) =
    (publishingHouseService: PublishingHouseService, genreService: GenreService) => {
      implicit val pub: PublishingHouseService = publishingHouseService
      implicit val gen: GenreService = genreService
      Form(
        mapping(
          "id" -> ignored[String]("-99"),
          "name" -> nonEmptyText.verifying("The fio can contain only letters!", name => isName(name)),
          "author" -> nonEmptyText.verifying("The fio can contain only letters!", name => isName(name)),
          "pub_year" -> optional(date),
          "pub_author" -> optional(text),
          "translator" -> optional(text),
          "comment" -> optional(text),
          "pub_house" -> optional(text),
          "genres" -> play.api.data.Forms.list(text)
        )(Book.apply)(Book.unapplyForm)
      )
    }

  def apply(id: String,
            name: String,
            author: String,
            pub_year: Option[Date],
            pub_author: Option[String],
            translator: Option[String],
            comment: Option[String],
            pub_house_id: Option[String],
            genres: List[String])(
             implicit publishingHouseService: PublishingHouseService,
             genreService: GenreService)
  : Book =
    new Book(BigInt(id),
      name,
      author,
      Option(pub_year.get.toInstant.atZone(ZoneId.systemDefault()).toLocalDate),
      pub_author,
      translator,
      comment,
      publishingHouseService.findById(pub_house_id.fold(BigInt(-99))(id => BigInt(id))),
      genres.map(g => genreService.findById(BigInt(g)))
        .map { case (Some(genre: Genre)) => genre })

  def unapplyForm(arg: Book):
  Option[(
    String,
      String,
      String,
      Option[Date],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      List[String])] =
    Option(
      arg.id.toString,
      arg.name,
      arg.author,
      Option(Date.from(arg.pub_year.get.atStartOfDay(ZoneId.systemDefault()).toInstant)),
      arg.pub_author,
      arg.translator,
      arg.comment,
      arg.pub_house.fold(Option.empty[String])(house => Option(house.id.toString)),
      arg.genres.map(_.id.toString))

  private def isName(fio: String) = fio.matches("""^([a-zA-Z\s])+$""")
}
