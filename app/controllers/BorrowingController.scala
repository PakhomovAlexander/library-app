package controllers

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import services.books.BookService
import services.borrowing.BorrowingService
import services.friends.FriendService
import views._


@Singleton
class BorrowingController @Inject()(borrowingService: BorrowingService,
                                    val messagesApi: MessagesApi)(
                                     implicit bookService: BookService,
                                     friendService: FriendService)
  extends Controller with I18nSupport {
  /**
    * This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.BorrowingController.list())

  /**
    * Describe the borrowing form (used in both edit and create screens).
    */
  val borrowingForm = Form(
    mapping(
      "id_friend" -> ignored[Long](-99L),
      "id_book" -> ignored[Long](-99L),
      "borrowing_date" -> date,
      "is_lost" -> optional(boolean),
      "is_damaged" -> optional(boolean),
      "return_date" -> optional(date),
      "commint" -> optional(text)
    )(Borrowing.apply)(Borrowing.unapplyForm)
  )

  // ------ Actions

  /**
    * Display the paginated list of friend.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on borrowing names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.borrowing.list(
      borrowingService.list(page = page, orderBy = orderBy, filter = s"%$filter%"),
      orderBy, filter
    ))
  }

  /**
    * Display the 'edit form' of a existing Borrowing.
    *
    * @param id_friend Id of the friend to edit
    * @param id_book   Id of the book to edit
    * @param date      Borrowing date
    */
  def edit(id_friend: Long, id_book: Long, date: String) = Action { implicit request =>
    borrowingService.findByPk(id_friend, id_book, LocalDate.parse(date)).map { borrowing =>
      Ok(html.borrowing.editForm(id_friend, id_book, LocalDate.parse(date), borrowingForm.fill(borrowing)))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id_friend Id of the friend to edit
    * @param id_book   Id of the book to edit
    * @param date      Borrowing date
    */
  def update(id_friend: Long, id_book: Long, date: String) = Action { implicit request =>
    borrowingForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.borrowing.editForm(id_friend, id_book, LocalDate.parse(date), formWithErrors)),
      borrowing => {
        borrowingService.update(borrowing)
        Home.flashing("success" -> s"The borrowing book ${borrowing.book.name} to ${borrowing.friend.fio} has been updated")
      }
    )
  }

  /**
    * Display the 'new borrowing form'.
    */
  def create = Action { implicit request =>
    Ok(html.borrowing.createForm(borrowingForm))
  }

  /**
    * Handle the 'new borrowing form' submission.
    */
  def save = Action { implicit request =>
    borrowingForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.borrowing.createForm(formWithErrors)),
      borrowing => {
        borrowingService.borrow(borrowing)
        Home.flashing("success" -> s"The borrowing book ${borrowing.book.name} to ${borrowing.friend.fio} has been updated")
      }
    )
  }

  /**
    * Handle friend deletion.
    */
  def delete(id_friend: Long, id_book: Long, date: String) = Action { implicit request =>
    borrowingService.findByPk(id_friend, id_book, LocalDate.parse(date)).fold(
      Home.flashing("error" -> "You don't have such borrowed book"))(
      brwd => {
        borrowingService.giveBack(brwd)
        Home.flashing("success" -> "You've given bool back!")
      })
  }
}