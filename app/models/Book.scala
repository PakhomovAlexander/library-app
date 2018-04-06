package models

import java.util.Date

case class Book(id: Long,
                name: String,
                author: String,
                pub_year: Option[Date],
                pub_author: Option[String],
                translator: Option[String],
                comment: Option[String],
                pub_house: Option[PublishingHouse],
                genre: Option[Genre])
