package services.publishingHouses

import models.PublishingHouse
import services.{BasicService, PageService}

trait PublishingHouseService extends BasicService[PublishingHouse] with PageService[PublishingHouse]
