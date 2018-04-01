package models


case class Friend(id: Long,
                  fio: String,
                  phoneNumber: String,
                  socialNumber: String,
                  email: String,
                  comment: String)

object Friend {

  var friends = Set(
    Friend(1, "Pakhomov Alexander Sergeevich", "8-800-555-35-35",
      "AlexanderPakhomov", "Sasha@icloud.com", "Best friend"),
    Friend(2, "Brusencev Ivan Ivanivich", "8-890-111-22-22",
      "", "Brus@icloud.com", "Good person"),
    Friend(3, "Shabanov Artemii Alexandrovich", "8-900-323-11-11",
      "artemii36", "Shopen@icloud.com", "Blck and wt"),
    Friend(4, "Test Testovich Testov", "8-800-555-33-33",
      "Testik", "Test@gmail.com", "Test comment"),
    Friend(5, "Testa Testovna Testova", "8-800-555-33-44",
      "Testichk@", "Test@icloud.com", "Test comment")
  )

  def findAll() = friends.toList.sortBy(_.fio)
  def findById(id: Long) = friends.find(_.id == id)
}
