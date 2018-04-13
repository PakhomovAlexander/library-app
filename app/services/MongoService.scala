package services

import models.Entity
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Filters.{equal, regex}
import org.mongodb.scala.model.Sorts.ascending
import MongoHelper._

abstract class MongoService[T: Entity](database: MongoDatabase) extends BasicService[T] with PageService[T] {

  val codecRegistry: CodecRegistry

  //val client: MongoClient = MongoClient()
  //val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[T]

  /**
    * Return a page of entities.
    *
    * @param page     Page to display
    * @param pageSize Number of friends per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[T] = {
    val offset = pageSize * page

    val entities = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val totalRows = collection.count().headResult()

    Page(entities, page, offset, totalRows)
  }

  /**
    * Return a list of all entities.
    *
    */
  override def findAll(): List[T] = {
    collection.find().results().toList
  }

  /**
    * Return an entity.
    *
    * @param id The entity id
    */
  override def findById(id: Long): Option[T] = {
    Some(collection.find(equal("_id", id)).first().headResult())
  }

  /**
    * Update an entity.
    *
    * @param id     The entity id
    * @param entity The entity values.
    */
  override def update(id: Long, entity: T): Unit = {
    collection.replaceOne(equal("_id", id), entity)
  }

  /**
    * Insert a new entity.
    *
    * @param entity The entity values.
    */
  override def insert(entity: T): Unit = {
    collection.insertOne(entity).results()

  }

  /**
    * Delete an entity.
    *
    * @param id Id of the entity to delete.
    */
  override def delete(id: Long): Unit = {
    collection.deleteOne(equal("_id", id)).results()
  }
}
