package models.books

import java.util.Date

case class Book(id: Long,
                name: String,
                author: String,
                pubYear: Option[Date],
                pubAuthor: Option[String],
                translator: Option[String],
                comment: Option[String],
                pubHouseName: Option[String],
                genre: Option[String])
