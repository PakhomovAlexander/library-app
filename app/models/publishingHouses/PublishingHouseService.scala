package models.publishingHouses

import models.services.{BasicService, PageService}

trait PublishingHouseService extends BasicService[PublishingHouse] with PageService[PublishingHouse]
