import com.google.inject.AbstractModule
import models.friends.{FriendService, FriendServiceH2Impl}

class Module extends AbstractModule {
  override def configure() = {

    bind(classOf[FriendService])
      .to(classOf[FriendServiceH2Impl])
  }
}
