name := "libraryApp"

version := "1.0"

scalaVersion := "2.11.11"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1"


      