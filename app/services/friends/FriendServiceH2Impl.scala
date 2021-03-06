package services.friends

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import models.Friend
import play.api.db.DBApi
import services.Page

@Singleton
class FriendServiceH2Impl @Inject()(dbapi: DBApi) extends FriendService {
  private val db = dbapi.database("default")

  /**
    * Parse a Friend from a ResultSet
    */
  private val simple = {
    get[Option[BigInt]]("friend.id") ~
      get[String]("friend.fio") ~
      get[Option[String]]("friend.phone_number") ~
      get[Option[String]]("friend.social_number") ~
      get[Option[String]]("friend.email") ~
      get[Option[String]]("friend.comment") map {
      case id ~ fio ~ phone_number ~ social_number ~ email ~ comment =>
        Friend(id, fio, phone_number, social_number, email, comment)
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
  override def findById(id: BigInt): Option[Friend] = db.withConnection { implicit connection =>
    SQL("select * from friend where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)
  }

  /**
    * Return a page of Friends.
    *
    * @param page     Page to display
    * @param pageSize Number of friend per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int = 0, pageSize: Int = 10, orderBy: String, filterBy: String = "fio", filter: String = "%"): Page[Friend] = {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val friends = SQL(
        """
          select * from friend
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
          select count(*) from friend
          where {filterBy} like {filter}
        """
      ).on(
        'filterBy -> filterBy,
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
  override def update(id: BigInt, friend: Friend): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          update friend
          set fio = {fio}, phone_number = {phone_number},
            social_number = {social_number}, email = {email}, comment = {comment}
          where id = {id}
        """
      ).on(
        'id -> id,
        'fio -> friend.fio,
        'phone_number -> friend.phone_number,
        'social_number -> friend.social_number,
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
            {fio}, {phone_number}, {social_number}, {email}, {comment}
          )
        """
      ).on(
        'fio -> friend.fio,
        'phone_number -> friend.phone_number,
        'social_number -> friend.social_number,
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
  override def delete(id: BigInt): Unit = {
    db.withConnection { implicit connection =>
      SQL("delete from friend where id = {id}").on('id -> id).executeUpdate()
    }
  }
}
