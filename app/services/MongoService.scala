package services

import models.Friend
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters.{equal, regex}
import org.mongodb.scala.model.Sorts.ascending

abstract class MongoService[T](database: MongoDatabase) extends BasicService[T] with PageService[T] {

  private val codecRegistry = fromRegistries(fromProviders(classOf[T]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  //val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[T] = database.getCollection("friends")

  /**
    * Return a page of Friends.
    *
    * @param page     Page to display
    * @param pageSize Number of friends per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Friend] = {
    val offset = pageSize * page

    val friends = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val totalRows = collection.count().headResult()

    Page(friends, page, offset, totalRows)

  }

  /**
    * Return a list of all friend.
    *
    */
  override def findAll(): List[Friend] = {
    collection.find().results().toList
  }

  /**
    * Return a friend.
    *
    * @param id The friend id
    */
  override def findById(id: Long): Option[Friend] = {
    Some(collection.find(equal("_id", id)).first().headResult())
  }

  /**
    * Update a friend.
    *
    * @param id     The friend id
    * @param entity The friend values.
    */
  override def update(id: Long, entity: Friend): Unit = {
    collection.replaceOne(equal("_id", id), entity)
  }

  /**
    * Insert a new Friend.
    *
    * @param entity The friend values.
    */
  override def insert(entity: Friend): Unit = {
    collection.insertOne(entity).results()

  }

  /**
    * Delete a friend.
    *
    * @param id Id of the friend to delete.
    */
  override def delete(id: Long): Unit = {
    collection.deleteOne(equal("_id", id)).results()
  }
}
