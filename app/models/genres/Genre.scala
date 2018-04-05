package models.genres

case class Genre(id: Long, name: String, parentGenre: Option[Genre])
