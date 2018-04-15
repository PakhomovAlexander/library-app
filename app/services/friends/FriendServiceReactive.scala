package services.friends

import javax.inject.Singleton
import models.Friend
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONRegex, Macros, document}
import services.Page

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor


@Singleton
class FriendServiceReactive @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends FriendService
with MongoController with ReactiveMongoComponents {

  case class MongoFriend(_id: BSONObjectID,
                    fio: String,
                    phone_number: Option[String],
                    social_number: Option[String],
                    email: Option[String],
                    comment: Option[String])

  private def toFriend(mongoFriend: MongoFriend):Friend = {
    new Friend(Option(BigInt(mongoFriend._id.stringify, 16)),
              mongoFriend.fio,
              mongoFriend.phone_number,
              mongoFriend.social_number,
              mongoFriend.email,
              mongoFriend.comment)
  }

  private def toMongoFriend(friend: Friend): MongoFriend = {
    var _id = new BSONObjectID
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

  private def toMongoFriend(id: BigInt): MongoFriend = {
    var _id = new BSONObjectID
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


  def collection: Future[BSONCollection] = database.map(
    _.collection[BSONCollection]("friends"))
  implicit def personWriter: BSONDocumentWriter[MongoFriend] = Macros.writer[MongoFriend]
  implicit def personReader: BSONDocumentReader[MongoFriend] = Macros.reader[MongoFriend]



  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Friend] = {
    val offset = pageSize * page

    val selector = BSONDocument(filterBy -> BSONRegex(filter, "i"))

    val future = collection.flatMap(_.find(selector)
      .sort(BSONDocument(orderBy -> 1))
      .skip(offset)
      .cursor[MongoFriend]()
      .collect[List](pageSize, Cursor.FailOnError[List[MongoFriend]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toFriend(f))


    val totalRows = Await.result(collection.flatMap(_.find().cursor[MongoFriend]()
      .collect[List](-1, Cursor.FailOnError[List[MongoFriend]]())), Duration.Inf)

    val result = Await.result(future, Duration.Inf).map(f => toFriend(f))

    Page(result, page, offset, totalRows)

  }


  override def findAll(): List[Friend] = {
    val future = collection.flatMap(_.find().cursor[MongoFriend]()
      .collect[List](-1, Cursor.FailOnError[List[MongoFriend]]())) // .FailOnError - обработчик иключения

    Await.result(future, Duration.Inf).map(f => toFriend(f))
  }

  override def findById(id: BigInt): Option[Friend] = {
    val future = collection.flatMap(_.find("_id" -> id).cursor[MongoFriend]()
      .collect[List](-1, Cursor.FailOnError[List[MongoFriend]]())) // .FailOnError - обработчик иключения

    Option(Await.result(future, Duration.Inf).map(f => toFriend(f)).head)
  }

  override def update(id: BigInt, entity: Friend): Unit = {
    val mongoFriend = toMongoFriend(entity)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.update(selector, mongoFriend))
  }

  override def insert(entity: Friend): Unit = {
    collection.flatMap(_.insert(toMongoFriend(entity)))
  }

  override def delete(id: BigInt): Unit = {
    val mongoFriend = toMongoFriend(id)

    val selector = document("_id" -> mongoFriend._id)

    collection.flatMap(_.remove(selector))
  }
}
