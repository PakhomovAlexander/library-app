package models.genres

case class Genre(id: Long, name: String, parent_genre: Option[Genre])
