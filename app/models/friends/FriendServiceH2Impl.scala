package models.friends

import javax.inject.{Inject, Singleton}

import anorm.{SQL, ~}
import anorm.SqlParser.{get, scalar}
import models.Page
import play.api.db.DBApi

@Singleton
class FriendServiceH2Impl @Inject()(dbapi: DBApi) extends FriendService {
  private val db = dbapi.database("default")

  /**
    * Parse a Friend from a ResultSet
    */
  private val simple = {
    get[Option[Long]]("friend.id") ~
      get[String]("friend.fio") ~
      get[Option[String]]("friend.phoneNumber") ~
      get[Option[String]]("friend.socialNumber") ~
      get[Option[String]]("friend.email") ~
      get[Option[String]]("friend.comment") map {
      case id ~ fio ~ phoneNumber ~ socialNumber ~ email ~ comment =>
        Friend(id, fio, phoneNumber, socialNumber, email, comment)
    }
  }

  /**
    * Return a list of all friend.
    *
    */
  override def findAll(): List[Friend] = db.withConnection { implicit connection =>
    SQL("select * from friend order by fio").as(simple *)
  }

  /**
    * Return a friend.
    *
    * @param id The friend id
    */
  override def findById(id: Long): Option[Friend] = db.withConnection { implicit connection =>
    SQL("select * from friend where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)
  }

  /**
    * Return a page of Friends.
    *
    * @param page     Page to display
    * @param pageSize Number of friends per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[Friend] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val friends = SQL(
        """
          select * from friend
          where fio like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offset,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(simple *)

      val totalRows = SQL(
        """
          select count(*) from friend
          where fio like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(friends, page, offset, totalRows)
    }
  }

  /**
    * Update a friend.
    *
    * @param id     The friend id
    * @param friend The friend values.
    */
  override def update(id: Long, friend: Friend): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          update friend
          set fio = {fio}, phoneNumber = {phoneNumber},
            socialNumber = {socialNumber}, email = {email}, comment = {comment}
          where id = {id}
        """
      ).on(
        'id -> id,
        'fio -> friend.fio,
        'phoneNumber -> friend.phoneNumber,
        'socialNumber -> friend.socialNumber,
        'email -> friend.email,
        'comment -> friend.comment
      ).executeUpdate()
    }
  }

  /**
    * Insert a new Friend.
    *
    * @param friend The friend values.
    */
  override def insert(friend: Friend): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into friend values (
            (select next value for friend_seq),
            {fio}, {phoneNumber}, {socialNumber}, {email}, {comment}
          )
        """
      ).on(
        'fio -> friend.fio,
        'phoneNumber -> friend.phoneNumber,
        'socialNumber -> friend.socialNumber,
        'email -> friend.email,
        'comment -> friend.comment
      ).executeUpdate()
    }
  }

  /**
    * Delete a friend.
    *
    * @param id Id of the friend to delete.
    */
  override def delete(id: Long): Unit = {
    db.withConnection { implicit connection =>
      SQL("delete from friend where id = {id}").on('id -> id).executeUpdate()
    }
  }
}
