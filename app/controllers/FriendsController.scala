package controllers

import javax.inject.{Inject, Singleton}

import models.friends.FriendService
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class FriendsController @Inject()(friendService: FriendService,
                                  cc: ControllerComponents
                                 ) extends AbstractController(cc) {
  def list = Action { implicit request =>
    val friends = friendService.findAll()
    Ok(views.html.friends.list(friends))
  }

  def show(id: Long) = Action { implicit request =>
    friendService.findById(id).map { product =>
      Ok(views.html.friends.details(product))
    }.getOrElse(NotFound)
  }
}
