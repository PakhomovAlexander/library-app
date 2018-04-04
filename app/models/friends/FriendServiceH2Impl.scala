package models.friends

import javax.inject.{Inject, Singleton}

import anorm.{SQL, ~}
import anorm.SqlParser.get
import play.api.db.DBApi

@Singleton
class FriendServiceH2Impl @Inject()(dbapi: DBApi) extends FriendService {
  /**
    * Parse a Friend from a ResultSet
    */
  val simple = {
    get[Long]("friend.id") ~
      get[String]("friend.fio") ~
      get[Option[String]]("friend.phoneNumber") ~
      get[Option[String]]("friend.socialNumber") ~
      get[Option[String]]("friend.email") ~
      get[Option[String]]("friend.comment") map {
      case id ~ fio ~ phoneNumber ~ socialNumber ~ email ~ comment =>
        Friend(id, fio, phoneNumber, socialNumber, email, comment)
    }
  }
  private val db = dbapi.database("default")

  override def findAll() = db.withConnection { implicit connection =>
    SQL("select * from friend order by fio").as(simple *)
  }

  override def findById(id: Long) = db.withConnection { implicit connection =>
    SQL("select * from friend where id = {id}")
      .on('id -> id)
      .as(simple.singleOpt)
  }
}
