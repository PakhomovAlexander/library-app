package models

import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, optional, _}
import services.genres.GenreService

case class Genre(id: BigInt, name: String, parent_genre: Option[Genre])

object Genre {
  /**
    * Describe the genre form (used in both edit and create screens).
    */
  lazy val genreForm: ((GenreService) => Form[Genre]) = (genreService: GenreService) => {
    implicit val gen: GenreService = genreService
    Form(
      mapping(
        "id" -> ignored[String]("-99"),
        "Name" -> nonEmptyText,
        "Parent" -> optional(text)
      )(Genre.apply)(Genre.unapplyForm)
    )
  }

  def apply(id: String, name: String, parent_id_opt: Option[String])(
    implicit genreService: GenreService): Genre = {
    new Genre(
      BigInt(id),
      name,
      parent_id_opt.fold(Option.empty[Genre])(p => genreService.findById(BigInt(p)))
    )
  }

  def unapplyForm(arg: Genre): Option[(String, String, Option[String])] = {
    Option(
      arg.id.toString,
      arg.name,
      arg.parent_genre.map(_.id.toString)
    )
  }
}