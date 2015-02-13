
name := "shortID"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions += "-feature"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.6",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

lazy val root = (project in file(".")).settings()
