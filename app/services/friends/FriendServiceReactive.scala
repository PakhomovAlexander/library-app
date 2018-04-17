package services.friends

import javax.inject.{Inject, Singleton}
import models.Friend
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


@Singleton
class FriendServiceReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends FriendService
  with MongoController with ReactiveMongoComponents {

  override def list(page: Int, pageSize: Int, orderBy: Int, filterBy: String = "fio", filter: String): Page[Friend] = {
    val offset = pageSize * page
    val filterReg = filter filter (_ != '%')

    val selector = BSONDocument(filterBy -> BSONRegex(s"(.*)$filterReg(.*)", "i"))

    val future = collection
      .flatMap(_.find(selector)
      .sort(BSONDocument(mapOrder(orderBy) -> 1))
      .skip(offset)
      .cursor[MongoFriend]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoFriend]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toFriend(f))


    val totalRows = Await.result(collection.flatMap(_.find(BSONDocument()).cursor[MongoFriend]()
      .collect[List](-1, Cursor.FailOnError[List[MongoFriend]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toFriend(f))

    Page(result, page, offset, totalRows.size)

  }

  private def toFriend(mongoFriend: MongoFriend): Friend = {
    new Friend(Option(BigInt(mongoFriend._id.stringify, 16)),
      mongoFriend.fio,
      mongoFriend.phone_number,
      mongoFriend.social_number,
      mongoFriend.email,
      mongoFriend.comment)
  }

  private def mapOrder(ord: Int) = ord match {
    case 1 => "_id"
    case 2 => "fio"
    case 3 => "phone_number"
    case 4 => "social_number"
    case 5 => "email"
    case 6 => "comment"
  }

  def collection: Future[BSONCollection] = database.map(
    _.collection[BSONCollection]("friends"))

  override def findAll(): List[Friend] = {
    val future = collection.flatMap(_.find(BSONDocument()).cursor[MongoFriend]()
      .collect[List](-1, Cursor.FailOnError[List[MongoFriend]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toFriend(f))
  }

  override def findById(id: BigInt): Option[Friend] = {
    val search = toMongoFriend(id)

    val future = collection.flatMap(_.find(BSONDocument("_id" -> search._id)).cursor[MongoFriend]()
      .collect[List](-1, Cursor.FailOnError[List[MongoFriend]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toFriend(f)).headOption
  }

  implicit def personWriter: BSONDocumentWriter[MongoFriend] = Macros.writer[MongoFriend]

  implicit def personReader: BSONDocumentReader[MongoFriend] = Macros.reader[MongoFriend]

  override def update(id: BigInt, entity: Friend): Unit = {
    val search = toMongoFriend(id)
    val mongoFriend = toMongoFriend(entity).copy(_id = search._id)

    val selector = document("_id" -> search._id)

    collection.flatMap(_.update(selector, mongoFriend))
  }

  override def insert(entity: Friend): Unit = {
    collection.flatMap(_.insert(toMongoFriend(entity)))
  }

  private def toMongoFriend(friend: Friend): MongoFriend = {
    var _id = BSONObjectID.generate()
    try {
      _id = BSONObjectID.parse(friend.id.get.bigInteger.toString(16)).get
    }
    catch {
      case _: Throwable => _id = BSONObjectID.generate()
    }

    MongoFriend(_id,
      friend.fio,
      friend.phone_number,
      friend.social_number,
      friend.email,
      friend.comment)
  }

  override def delete(id: BigInt): Unit = {
    val mongoFriend = toMongoFriend(id)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.remove(selector))
  }

  private def toMongoFriend(id: BigInt): MongoFriend = {
    var _id = BSONObjectID.generate()
    try {
      _id = BSONObjectID.parse(id.bigInteger.toString(16)).get
    }
    catch {
      case _: Throwable => _id = BSONObjectID.generate()
    }

    MongoFriend(_id,
      "",
      Option.empty,
      Option.empty,
      Option.empty,
      Option.empty)
  }

  case class MongoFriend(_id: BSONObjectID,
                         fio: String,
                         phone_number: Option[String],
                         social_number: Option[String],
                         email: Option[String],
                         comment: Option[String])
}
