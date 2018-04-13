package controllers

import javax.inject.{Inject, Singleton}

import models.Genre
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import services.friends.FriendService
import services.genres.GenreService
import views._


@Singleton
class GenreController @Inject()(implicit genreService: GenreService,
                                  val messagesApi: MessagesApi)
  extends Controller with I18nSupport {
  /**
    * This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.GenreController.list())

  /**
    * Describe the genre form (used in both edit and create screens).
    */
  val genreForm = Form(
    mapping(
      "id" -> ignored[Long](-99L),
      "name" -> nonEmptyText,
      "parents" -> optional(longNumber)
    )(Genre.apply)(Genre.unapplyForm)
  )

  // ------ Actions

  /**
    * Display the paginated list of genres
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on friend names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.genre.list(
      genreService.list(page = page, orderBy = orderBy, filter = s"%$filter%"),
      orderBy, filter
    ))
  }

  /**
    * Display the 'edit form' of a existing Genre.
    *
    * @param id Id of the genre to edit
    */
  def edit(id: Long) = Action { implicit request =>
    genreService.findById(id).map { genre =>
      Ok(html.genre.editForm(id, genreForm.fill(genre)))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the genre to edit
    */
  def update(id: Long) = Action { implicit request =>
    genreForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.genre.editForm(id, formWithErrors)),
      genre => {
        genreService.update(id, genre)
        Home.flashing("success" -> s"Genre ${genre.name} has been updated")
      }
    )
  }

  /**
    * Display the 'new genre form'.
    */
  def create = Action { implicit request =>
    Ok(html.genre.createForm(genreForm))
  }

  /**
    * Handle the 'new genre form' submission.
    */
  def save = Action { implicit request =>
    genreForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.genre.createForm(formWithErrors)),
      genre => {
        genreService.insert(genre)
        Home.flashing("success" -> s"Genre ${genre.name} has been created")
      }
    )
  }

  /**
    * Handle genre deletion.
    */
  def delete(id: Long) = Action { implicit request =>
    genreService.delete(id)
    Home.flashing("success" -> "Genre has been deleted")
  }
}
