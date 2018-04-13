package controllers

import javax.inject.{Inject, Singleton}

import models.Friend
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import services.friends.FriendService
import views._


@Singleton
class FriendsController @Inject()(friendService: FriendService,
                                  val messagesApi: MessagesApi)
  extends Controller with I18nSupport {
  /**
    * This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.FriendsController.list())

  /**
    * Describe the friend form (used in both edit and create screens).
    */
  val friendForm = Form(
    mapping(
      "id" -> ignored(None:Option[Long]),
      "FIO" -> nonEmptyText,
      "Phone number" -> optional(text),
      "Social networks" -> optional(text),
      "Email" -> optional(text),
      "Comment" -> optional(text)
    )(Friend.apply)(Friend.unapply)
  )

  // ------ Actions

  /**
    * Display the paginated list of friend.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on friend names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.friends.list(
      friendService.list(page = page, orderBy = orderBy, filter = s"%$filter%"),
      orderBy, filter
    ))
  }

  /**
    * Display the 'edit form' of a existing Friend.
    *
    * @param id Id of the friend to edit
    */
  def edit(id: Long) = Action { implicit request =>
    friendService.findById(id).map { friend =>
      Ok(html.friends.editForm(id, friendForm.fill(friend)))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the friend to edit
    */
  def update(id: Long) = Action { implicit request =>
    friendForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.friends.editForm(id, formWithErrors)),
      friend => {
        friendService.update(id, friend)
        Home.flashing("success" -> s"Friend ${friend.fio} has been updated")
      }
    )
  }

  /**
    * Display the 'new friend form'.
    */
  def create = Action { implicit request =>
    Ok(html.friends.createForm(friendForm))
  }

  /**
    * Handle the 'new friend form' submission.
    */
  def save = Action { implicit request =>
    friendForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.friends.createForm(formWithErrors)),
      friend => {
        friendService.insert(friend)
        Home.flashing("success" -> s"Friend ${friend.fio} has been created")
      }
    )
  }

  /**
    * Handle friend deletion.
    */
  def delete(id: Long) = Action { implicit request =>
    friendService.delete(id)
    Home.flashing("success" -> "Friend has been deleted")
  }
}
