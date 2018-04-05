package models.genres

import models.services.{BasicService, PageService}

trait GenreService extends BasicService[Genre] with PageService[Genre]
