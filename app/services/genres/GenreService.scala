package services.genres

import models.Genre
import services.{BasicService, PageService}

trait GenreService extends BasicService[Genre] with PageService[Genre]
