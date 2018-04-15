package services.books

import models.Book
import services.{BasicService, PageService}

trait BookService extends BasicService[Book] with PageService[Book]