package models.friends

import models.services.{BasicService, PageService}


trait FriendService extends BasicService[Friend] with PageService[Friend]
