import com.google.inject.AbstractModule
import services.books.{BookService, BookServiceH2Impl}
import services.borrowing.{BorrowingService, BorrowingServiceH2Impl}
import services.friends.{FriendService, FriendServiceH2Impl}
import services.genres.{GenreService, GenreServiceH2Impl}
import services.publishingHouses.{PublishingHouseService, PublishingHouseServiceH2Impl}

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[FriendService])
      .to(classOf[FriendServiceH2Impl])

    bind(classOf[PublishingHouseService])
      .to(classOf[PublishingHouseServiceH2Impl])

    bind(classOf[GenreService])
      .to(classOf[GenreServiceH2Impl])

    bind(classOf[BookService])
      .to(classOf[BookServiceH2Impl])

    bind(classOf[BorrowingService])
      .to(classOf[BorrowingServiceH2Impl])
  }
}
