package controllers

import javax.inject.{Inject, Singleton}

import models.Friend
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class FriendsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def list = Action { implicit request =>
    val friends = Friend.findAll()
    Ok(views.html.friends.list(friends))
  }

  def show(id: Long) = Action { implicit request =>
    Friend.findById(id).map { product =>
      Ok(views.html.friends.details(product))
    }.getOrElse(NotFound)
  }
}
