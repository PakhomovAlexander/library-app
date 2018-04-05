package models.books

import java.util.Date

import models.genres.Genre
import models.publishingHouses.PublishingHouse

case class Book(id: Long,
                name: String,
                author: String,
                pubYear: Option[Date],
                pubAuthor: Option[String],
                translator: Option[String],
                comment: Option[String],
                pubHouseName: Option[PublishingHouse],
                genre: Option[Genre])
