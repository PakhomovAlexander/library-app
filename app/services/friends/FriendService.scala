package services.friends

import models.Friend
import services.{BasicService, PageService}


trait FriendService extends BasicService[Friend] with PageService[Friend]
