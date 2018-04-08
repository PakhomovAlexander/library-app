package models

case class Genre(id: Long, name: String, parent_genre: Option[Genre])
