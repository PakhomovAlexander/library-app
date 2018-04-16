package services.genres

import javax.inject.{Inject, Singleton}

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import models.Genre
import play.api.db.DBApi
import services.Page

@Singleton
class GenreServiceH2Impl @Inject()(dbapi: DBApi) extends GenreService {
  private val db = dbapi.database("default")

  /**
    * Parse a Genre from a ResultSet (recursive!)
    */
  private val simple = {
    get[Long]("genre.id") ~
      get[String]("genre.name") ~
      get[Option[Long]]("genre.id_parent_genre") map {
      case id ~ name ~ id_parent_genre =>
        Genre(id, name, genreBy(id = id_parent_genre))
    }
  }

  /**
    * Return a list of all genres.
    *
    */
  override def findAll(): List[Genre] = db.withConnection { implicit connection =>
    SQL("select * from genre order by name").as(simple *)
  }

  /**
    * Return a genre.
    *
    * @param id The genre id
    */
  override def findById(id: BigInt): Option[Genre] = db.withConnection { implicit connection =>
    SQL("select * from genre where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)
  }

  /**
    * Return a page of Genres.
    *
    * @param page     Page to display
    * @param pageSize Number of genres per page
    * @param orderBy  Book property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: String, filterBy: String = "name", filter: String = "%"): Page[Genre] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val genres = SQL(
        """
          select * from genre
          where """ + filterBy + """ like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offset,
        'filterBy -> filterBy,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(simple *)

      val totalRows = SQL(
        """
          select count(*) from genre
          where {filterBy} like {filter}
        """
      ).on(
        'filterBy -> filterBy,
        'filter -> filter
      ).as(scalar[Long].single)

      Page(genres, page, offset, totalRows)
    }
  }

  /**
    * Update a genre.
    *
    * @param id    The genre id
    * @param genre The genre value.
    */
  override def update(id: BigInt, genre: Genre): Unit = {
    db.withConnection { implicit connection =>
      genre.parent_genre.fold(
      SQL(
        """
              update genre
              set name = {name}
              where id = {id}
            """
      ).on(
        'name -> genre.name,
        'id -> id
      ).executeUpdate())( parent =>
        SQL(
          """
              update genre
              set name = {name}, id_parent_genre = {id_parent_genre}
              where id = {id}
            """
          ).on(
          'name -> genre.name,
          'id_parent_genre -> parent.id,
          'id -> id
      ).executeUpdate())
    }
  }

  /**
    * Insert a new Genre.
    *
    * @param genre The genre value.
    */
  override def insert(genre: Genre): Unit = {
    db.withConnection { implicit connection =>
      genre.parent_genre.fold(SQL(
        """ insert into genre(id, name) values (
          |(select next value for genre_seq), {name}) """.stripMargin
      ).on( 'name -> genre.name).executeUpdate())(  parent =>
        SQL(
          """ insert into genre(id, name, id_parent_genre) values (
            |(select next value for genre_seq), {name}, {id_parent_genre} ) """.stripMargin
        ).on( 'name -> genre.name, 'id_parent_genre -> parent.id).executeUpdate()
      )
    }
  }

  /**
    * Delete a genre.
    *
    * @param id Id of the genre to delete.
    */
  override def delete(id: BigInt): Unit = {
    db.withConnection { implicit connection =>
      SQL("delete from genre where id = {id}").on('id -> id).executeUpdate()
    }
  }

  private def genreBy(id: Option[Long]): Option[Genre] =
    if (id.isEmpty) {
      Option.empty
    } else {
      db.withConnection { implicit connection =>
        SQL("select * from genre where id = {id}")
          .on('id -> id.get)
          .as(simple.singleOpt)
      }
    }

  /**
    * Returns Lins of book's genres
    * @param bookId The Bok id
    * @return
    */
  def genres(bookId: BigInt): List[Genre] = db.withConnection { implicit connection =>
    SQL("select * from genre where id = {id}")
      .on('id -> bookId)
      .as(simple *)
  }


}
