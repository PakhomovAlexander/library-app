package services.publishingHouses

import models.PublishingHouse
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters.{equal, regex}
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import services.MongoHelper._
import services.Page

class PublishingHouseMongoImpl extends PublishingHouseService {

  private val codecRegistry =
    fromRegistries(fromProviders(classOf[PublishingHouse]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[PublishingHouse] = database.getCollection("publishingHouses")

  /**
    * Return a page of PublishingHouses.
    *
    * @param page     Page to display
    * @param pageSize Number of publishing houses per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[PublishingHouse] = {
    val offset = pageSize * page

    val publishingHouses = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val totalRows = collection.count().headResult()

    Page(publishingHouses, page, offset, totalRows)

  }

  /**
    * Return a list of all publishing houses.
    *
    */
  override def findAll(): List[PublishingHouse] = {
    collection.find().results().toList
  }

  /**
    * Return a publishing house.
    *
    * @param id The publishing house id
    */
  override def findById(id: Long): Option[PublishingHouse] = {
    Some(collection.find(equal("_id", id)).first().headResult())
  }

  /**
    * Update a publishing house.
    *
    * @param id     The publishing house id
    * @param entity The publishing house values.
    */
  override def update(id: Long, entity: PublishingHouse): Unit = {
    collection.replaceOne(equal("_id", id), entity)
  }

  /**
    * Insert a new publishing house.
    *
    * @param entity The publishing house values.
    */
  override def insert(entity: PublishingHouse): Unit = {
    collection.insertOne(entity).results()

  }

  /**
    * Delete a publishing house.
    *
    * @param id Id of the publishing house to delete.
    */
  override def delete(id: Long): Unit = {
    collection.deleteOne(equal("_id", id)).results()
  }
}
