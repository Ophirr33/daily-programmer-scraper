name := "daily-programmer-scraper"

version := "0.1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.jcenterRepo

libraryDependencies += "net.dean.jraw" % "JRAW" % "0.9.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.0"

fork in run := true