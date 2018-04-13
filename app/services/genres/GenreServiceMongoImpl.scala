package services.genres

import models.Genre
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters.{equal, regex}
import org.mongodb.scala.model.Sorts.ascending
import services.Page
import services.MongoHelper._

class GenreServiceMongoImpl extends GenreService {
  private val codecRegistry = fromRegistries(fromProviders(classOf[Genre]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[Genre] = database.getCollection("genres")

  /**
    * Return a page of Genres.
    *
    * @param page     Page to display
    * @param pageSize Number of genres per page
    * @param orderBy  Friend property used for sorting
    * @param filter   Filter applied on the name column
    */
  override def list(page: Int, pageSize: Int, orderBy: String, filterBy: String, filter: String): Page[Genre] = {
    val offset = pageSize * page

    val friends = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val totalRows = collection.count().headResult()

    Page(friends, page, offset, totalRows)

  }

  /**
    * Return a list of all genres.
    *
    */
  override def findAll(): List[Genre] = {
    collection.find().results().toList
  }

  /**
    * Return a genre.
    *
    * @param id The genre id
    */
  override def findById(id: Long): Option[Genre] = {
    Some(collection.find(equal("_id", id)).first().headResult())
  }

  /**
    * Update a genre.
    *
    * @param id     The genre id
    * @param entity The genre values.
    */
  override def update(id: Long, entity: Genre): Unit = {
    collection.replaceOne(equal("_id", id), entity)
  }

  /**
    * Insert a new Friend.
    *
    * @param entity The genre values.
    */
  override def insert(entity: Genre): Unit = {
    collection.insertOne(entity).results()

  }

  /**
    * Delete a genre.
    *
    * @param id Id of the genre to delete.
    */
  override def delete(id: Long): Unit = {
    collection.deleteOne(equal("_id", id)).results()
  }

  /**
    * Returns Lins of book's genres
    * @param bookId The Bok id
    * @return
    */
  def genres(bookId: Long): List[Genre] = {
    collection.find(equal("_id", bookId)).first().results().toList
  }
}
