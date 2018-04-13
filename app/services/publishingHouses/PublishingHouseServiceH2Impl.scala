package services.publishingHouses

import javax.inject.{Inject, Singleton}

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import models.PublishingHouse
import play.api.db.DBApi
import services.Page

@Singleton
class PublishingHouseServiceH2Impl @Inject() (dbapi: DBApi) extends PublishingHouseService {
  private val db = dbapi.database("default")

  /**
    * Parse a Publishing house from a ResultSet
    */
  private val simple = {
    get[Long]("publishing_house.id") ~
      get[String]("publishing_house.name") map {
      case id ~ name =>
        PublishingHouse(id, name)
    }
  }

  /**
    * Return a list of all publishing houses.
    *
    */
  override def findAll(): List[PublishingHouse] = db.withConnection { implicit connection =>
    SQL("select * from publishing_house order by name").as(simple *)
  }

  /**
    * Return a Publishing house.
    *
    * @param id The publishing house id
    */
  override def findById(id: Long): Option[PublishingHouse] = db.withConnection { implicit connection =>
    SQL("select * from publishing_house where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)
  }

  /**
    * Return a page of Publishing house.
    *
    * @param page     Page to display
    * @param pageSize Number of houses per page
    * @param orderBy  Publishing house property used for sorting
    * @param filter   Filter applied on the filter column
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1,
                    filterBy: String = "name", filter: String = "%"): Page[PublishingHouse] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val houses = SQL(
        """
          select * from publishing_house
          where {filterBy} like {filter}
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
          select count(*) from publishing_house
          where {filterBy} like {filter}
        """
      ).on(
        'filterBy -> filterBy,
        'filter -> filter
      ).as(scalar[Long].single)

      Page(houses, page, offset, totalRows)
    }
  }

  /**
    * Update a publishing house.
    *
    * @param id              The house id
    * @param publishingHouse The house values.
    */
  override def update(id: Long, publishingHouse: PublishingHouse): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          update publishing_house
          set name = {name}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> publishingHouse.name
      ).executeUpdate()
    }
  }

  /**
    * Insert a new Publishing house.
    *
    * @param publishingHouse The friend values.
    */
  override def insert(publishingHouse: PublishingHouse): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into publishing_house values (
            (select next value for publishing_house_seq),
            {name}
          )
        """
      ).on(
        'name -> publishingHouse.name
      ).executeUpdate()
    }
  }

  /**
    * Delete a publishing house.
    *
    * @param id Id of the house to delete.
    */
  override def delete(id: Long): Unit = {
    db.withConnection { implicit connection =>
      SQL("delete from publishing_house where id = {id}").on('id -> id).executeUpdate()
    }
  }
}
