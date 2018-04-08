package services.books

import models.{Book, Genre}
import services.{BasicService, PageService}

trait BookService extends BasicService[Book] with PageService[Book]