import com.google.inject.AbstractModule
import services.books.{BookService, BookServiceH2Impl, BookServiceReactive}
import services.borrowing.{BorrowingService, BorrowingServiceH2Impl, BorrowingServiceReactive}
import services.friends.{FriendService, FriendServiceH2Impl, FriendServiceReactive}
import services.genres.{GenreService, GenreServiceH2Impl, GenreServiceReactive}
import services.publishingHouses.{PublishingHouseReactive, PublishingHouseService, PublishingHouseServiceH2Impl}

class Module extends AbstractModule {
  override def configure() = {
//    bind(classOf[FriendService])
//      .to(classOf[FriendServiceH2Impl])
//
//    bind(classOf[PublishingHouseService])
//      .to(classOf[PublishingHouseServiceH2Impl])
//
//    bind(classOf[GenreService])
//      .to(classOf[GenreServiceH2Impl])
//
//    bind(classOf[BookService])
//      .to(classOf[BookServiceH2Impl])
//
//    bind(classOf[BorrowingService])
//      .to(classOf[BorrowingServiceH2Impl])

    bind(classOf[FriendService])
      .to(classOf[FriendServiceReactive])

    bind(classOf[PublishingHouseService])
      .to(classOf[PublishingHouseReactive])

    bind(classOf[GenreService])
      .to(classOf[GenreServiceReactive])

    bind(classOf[BookService])
      .to(classOf[BookServiceReactive])

    bind(classOf[BorrowingService])
      .to(classOf[BorrowingServiceReactive])
  }
}
