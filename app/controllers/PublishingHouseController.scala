package controllers

import javax.inject.{Inject, Singleton}

import models.PublishingHouse
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, _}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Result}
import services.publishingHouses.PublishingHouseService
import views.html

@Singleton
class PublishingHouseController @Inject()(publishingHouseService: PublishingHouseService,
                                          val messagesApi: MessagesApi) extends Controller with I18nSupport {
  /**
    * This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.PublishingHouseController.list())

  /**
    * Describe the publishing house form (used in both edit and create screens).
    */
  val publishingHouseForm = Form(
    mapping(
      "id" -> ignored[Long](-99),
      "name" -> nonEmptyText
    )(PublishingHouse.apply)(PublishingHouse.unapply)
  )

  // ------ Actions

  /**
    * Display the paginated list of houses.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on house names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.publishingHouse.list(
      publishingHouseService.list(page = page, orderBy = orderBy, filterBy = "name", filter = s"%$filter%"),
      orderBy, filter
    ))
  }

  /**
    * Display the 'edit form' of a existing Publishing House.
    *
    * @param id Id of the house to edit
    */
  def edit(id: Long) = Action { implicit request =>
    publishingHouseService.findById(id).map { house =>
      Ok(html.publishingHouse.editForm(id, publishingHouseForm.fill(house)))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the house to edit
    */
  def update(id: Long) = Action { implicit request =>
    publishingHouseForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.publishingHouse.editForm(id, formWithErrors)),
      house => {
        publishingHouseService.update(id, house)
        Home.flashing("success" -> s"Publishing house ${house.name} has been updated")
      }
    )
  }

  /**
    * Display the 'new publishing house form'.
    */
  def create = Action { implicit request =>
    Ok(html.publishingHouse.createForm(publishingHouseForm))
  }

  /**
    * Handle the 'new publishing house form' submission.
    */
  def save = Action { implicit request =>
    publishingHouseForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.publishingHouse.createForm(formWithErrors)),
      house => {
        publishingHouseService.insert(house)
        Home.flashing("success" -> s"Publishing house ${house.name} has been created")
      }
    )
  }

  /**
    * Handle publishing house deletion.
    */
  def delete(id: Long) = Action { implicit request =>
    publishingHouseService.delete(id)
    Home.flashing("success" -> "Publishing house has been deleted")
  }
}
