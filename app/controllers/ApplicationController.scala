package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc._


@Singleton
class ApplicationController @Inject()() extends Controller {
  def index = Action { implicit request =>
    Redirect(routes.FriendsController.list())
  }
}
