package controllers

import javax.inject.{Inject, Singleton}

import models.Genre.genreForm
import play.api.i18n._
import play.api.mvc._
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
  val form = genreForm(genreService)
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
      Ok(html.genre.editForm(id, form.fill(genre), genreService.findAll()))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the genre to edit
    */
  def update(id: Long) = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(html.genre.editForm(id, formWithErrors, genreService.findAll())),
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
    Ok(html.genre.createForm(form, genreService.findAll()))
  }

  /**
    * Handle the 'new genre form' submission.
    */
  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(html.genre.createForm(formWithErrors, genreService.findAll())),
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
