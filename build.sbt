name := "libraryApp"

version := "1.0"

scalaVersion := "2.11.11"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers ++= Seq(
  "webjars"    at "http://webjars.github.com/m2"
)


lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery")
libraryDependencies += "org.webjars" % "font-awesome" % "4.7.0"
libraryDependencies += "org.webjars" % "bootstrap-datepicker" % "1.4.0" exclude("org.webjars", "bootstrap")
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies ++= Seq(
  "org.webjars"               %% "webjars-play"       % "2.3.0",
  "org.webjars"               % "jquery"              % "1.8.3"
)