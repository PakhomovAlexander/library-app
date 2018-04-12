package models

import services.genres.GenreService

case class Genre(id: Long, name: String, parent_genre: Option[Genre])

object Genre {
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