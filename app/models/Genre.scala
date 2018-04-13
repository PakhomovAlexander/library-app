package models

import play.api.data.Form
import play.api.data.Forms.{ignored, longNumber, mapping, optional, _}
import services.genres.GenreService

case class Genre(id: Long, name: String, parent_genre: Option[Genre]) {
  override def toString: String = s"$name ${parent_genre.fold("")(genre => s"// $genre")}"
}

object Genre {
  /**
    * Describe the genre form (used in both edit and create screens).
    */
  lazy val genreForm: ((GenreService) => Form[Genre]) = (genreService: GenreService) => {
    implicit val gen: GenreService = genreService
    Form(
      mapping(
        "id" -> ignored[Long](-99L),
        "Name" -> nonEmptyText,
        "Parent" -> optional(longNumber)
      )(Genre.apply)(Genre.unapplyForm)
    )
  }

  def apply(id: Long, name: String, parent_id_opt: Option[Long])(
    implicit genreService: GenreService): Genre = {
    new Genre(
      id, name, if (parent_id_opt.isDefined) genreService.findById(parent_id_opt.get) else Option.empty[Genre]
    )
  }

  def unapplyForm(arg: Genre): Option[(Long, String, Option[Long])] = {
    Option(arg.id, arg.name, if (arg.parent_genre.isDefined) Option(arg.parent_genre.get.id) else Option.empty[Long])
  }
}