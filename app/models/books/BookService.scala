package models.books

import models.services.{BasicService, PageService}

trait BookService extends BasicService[Book] with PageService[Book]
