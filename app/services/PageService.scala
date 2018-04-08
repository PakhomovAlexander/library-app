package services

import models.Page

/**
  * Trait provide pagination behavior
  * @tparam T  Entity type
  */
trait PageService[T] {
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filterBy: String = "id", filter: String = "%"): Page[T]
}
