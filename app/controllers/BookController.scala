package controllers

import javax.inject.{Inject, Singleton}
import models.Book.bookForm
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Result}
import services.books.BookService
import services.genres.GenreService
import services.publishingHouses.PublishingHouseService
import views.html

@Singleton
class BookController @Inject()(bookService: BookService,
                               val messagesApi: MessagesApi)(
                                implicit genreService: GenreService,
                                publishingHouseService: PublishingHouseService
                              ) extends Controller with I18nSupport {
  /**
    * This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.BookController.list())

  // ------ Actions

  val bF = bookForm(publishingHouseService, genreService)
  /**
    * Display the paginated list of books.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on house names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.book.list(
      bookService.list(page = page, orderBy = orderBy, filterBy = "name", filter = s"%$filter%"),
      orderBy, filter
    ))
  }

  /**
    * Display the 'edit form' of a existing Book.
    *
    * @param id Id of the book to edit
    */
  def edit(id: String) = Action { implicit request =>
    bookService.findById(BigInt(id)).map { book =>
      Ok(html.book.editForm(id, bF.fill(book), genreService.findAll(),
        book.genres, publishingHouseService.findAll()))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the book to edit
    */
  def update(id: String) = Action { implicit request =>
    bF.bindFromRequest.fold(
      formWithErrors => BadRequest(html.book.editForm(id, formWithErrors, genreService.findAll(),
        bookService.findById(BigInt(id)).get.genres, publishingHouseService.findAll())),
      book => {
        bookService.update(BigInt(id), book)
        Home.flashing("success" -> s"Book ${book.name} has been updated")
      }
    )
  }

  /**
    * Display the 'new book form'.
    */
  def create = Action { implicit request =>
    Ok(html.book.createForm(bF, genreService.findAll(), publishingHouseService.findAll()))
  }

  /**
    * Handle the 'new book form' submission.
    */
  def save = Action { implicit request =>
    bF.bindFromRequest.fold(
      formWithErrors => BadRequest(html.book.createForm(formWithErrors, genreService.findAll(), publishingHouseService.findAll())),
      book => {
        bookService.insert(book)
        Home.flashing("success" -> s"Book ${book.name} has been created")
      }
    )
  }

  /**
    * Handle book deletion.
    */
  def delete(id: String) = Action { implicit request =>
    bookService.delete(BigInt(id))
    Home.flashing("success" -> "Book has been deleted")
  }
}
