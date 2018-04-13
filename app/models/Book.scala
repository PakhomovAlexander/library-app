package models

import java.time.{LocalDate, ZoneId}
import java.util.Date

import services.genres.GenreService
import services.publishingHouses.PublishingHouseService

case class Book(id: Long,
                name: String,
                author: String,
                pub_year: Option[LocalDate],
                pub_author: Option[String],
                translator: Option[String],
                comment: Option[String],
                pub_house: Option[PublishingHouse],
                genres: List[Genre])

object Book {
  def apply(id: Long,
            name: String,
            author: String,
            pub_year: Option[Date],
            pub_author: Option[String],
            translator: Option[String],
            comment: Option[String],
            pub_house_id: Option[Long],
            genres: List[Long])(
             implicit publishingHouseService: PublishingHouseService,
             genreService: GenreService)
  : Book =
    new Book(id,
      name,
      author,
      Option(pub_year.get.toInstant.atZone(ZoneId.systemDefault()).toLocalDate),
      pub_author,
      translator,
      comment,
      publishingHouseService.findById(pub_house_id.getOrElse(-99)),
      genres.map(genreService.findById).map { case (Some(genre)) => genre })

  def unapplyForm(arg: Book):
  Option[(
    Long,
      String,
      String,
      Option[Date],
      Option[String],
      Option[String],
      Option[String],
      Option[Long],
      List[Long])] =
    Option(
      arg.id,
      arg.name,
      arg.author,
      Option(Date.from(arg.pub_year.get.atStartOfDay(ZoneId.systemDefault()).toInstant)),
      arg.pub_author,
      arg.translator,
      arg.comment,
      arg.pub_house.fold(Option.empty[Long])(house => Option(house.id)),
      arg.genres.map(_.id))
}
