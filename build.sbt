name := "thesaurus-api"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.5.14",
	"com.typesafe.akka" %% "akka-testkit" % "2.5.14" % Test
)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-http" % "10.1.3",
	"com.typesafe.akka" %% "akka-stream" % "2.5.14"
)

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.2.0"


