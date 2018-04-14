package services.genres

import models.Genre
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters.{equal, regex}
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import services.MongoHelper._
import services.Page

class GenreServiceMongoImpl extends GenreService {

  case class GenreMongo(id: Long, name: String, parent_genre: Option[Long])

  private def GenreMongoToGenre(genreMongo: GenreMongo): Genre = {
      new Genre(
        genreMongo.id,
        genreMongo.name,
        genreBy(genreMongo.parent_genre)
      )
  }

  private val codecRegistry = fromRegistries(fromProviders(classOf[GenreMongo]), DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase("library").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[GenreMongo] = database.getCollection("genres")

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

    val genresMongo = collection.find()
      .filter(regex(filterBy, filter))
      .sort(ascending(orderBy))
      .skip(offset).limit(pageSize).results().toList

    val genres: List[Genre] = genresMongo.map(g => GenreMongoToGenre(g))

    val totalRows = collection.count().headResult()

    Page(genres, page, offset, totalRows)

  }

  /**
    * Return a list of all genres.
    *
    */
  override def findAll(): List[Genre] = {
    collection.find().results().toList.map(g => GenreMongoToGenre(g))
  }

  /**
    * Return a genre.
    *
    * @param id The genre id
    */
  override def findById(id: Long): Option[Genre] = {
    val opt = Option(collection.find(equal("id", id)).first().headResult())
    if (opt.isEmpty)
      Option.empty
    else
      Option(GenreMongoToGenre(opt.get))
  }

  /**
    * Update a genre.
    *
    * @param id     The genre id
    * @param entity The genre values.
    */
  override def update(id: Long, entity: Genre): Unit = {
    collection.updateOne(equal("id", id), set("name", entity.name))
  }

  /**
    * Insert a new Genre.
    *
    * @param entity The genre values.
    */
  override def insert(entity: Genre): Unit = {

    val mongoGenre = GenreMongo( entity.id, entity.name, Option(entity.parent_genre.get.id))

    collection.insertOne(mongoGenre).results()

  }

  /**
    * Delete a genre.
    *
    * @param id Id of the genre to delete.
    */
  override def delete(id: Long): Unit = {
    collection.deleteOne(equal("id", id)).results()
  }

  private def genreBy(id: Option[Long]): Option[Genre] = {
    if (id.isEmpty) {
      Option.empty
    } else {
      Some(GenreMongoToGenre(collection.find(equal("id", id)).headResult()))
    }
  }



  /**
    * Returns Lins of book's genres
    * @param bookId The Bok id
    * @return
    */
  def genres(bookId: Long): List[Genre] = {
    collection.find(equal("id", bookId)).first().results().toList.map(g => GenreMongoToGenre(g))
  }


}
