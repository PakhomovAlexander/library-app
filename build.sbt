name := "libraryApp"
 
version := "1.0" 
      
lazy val `libraryapp` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )
libraryDependencies += evolutions
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      