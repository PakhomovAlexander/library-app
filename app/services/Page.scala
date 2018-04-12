package services

/**
  * Helper for pagination.
  */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}
