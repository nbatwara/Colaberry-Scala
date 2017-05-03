name := "AkkaTest"


version := "1.0"

scalaVersion := "2.12.2"

val akkaVersion = "2.4.12"

libraryDependencies ++= Seq(
  // Change this to another test framework if you prefer
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.spotify" % "docker-client" % "3.5.13"
)




//name := "KafkaFromFile"
//
//lazy val commonSettings = Seq(
//
//  version := "0.1.0",
//  scalaVersion := "2.12.1"
//)
//
//lazy val root = (project in file(".")).
//  aggregate(mainFile,server)
//
//lazy val mainFile = (project in file(".")).
//  settings(commonSettings:_*)
////  packageArchetype.java_server
//lazy val server = {
// // import com.typesafe.sbt.packager.docker._
//  Project(
//    id = "server",
//    base = file("server"),
//    settings = commonSettings ++ Seq(
//      mainClass in Compile := Some("com.packt.masteringakka.bookstore.server.Server"),
//      dockerCommands := dockerCommands.value.filterNot {
//        // ExecCmd is a case class, and args is a varargs variable, so you need to bind it with @
//        case Cmd("USER", args@_*) => true
//        // dont filter the rest
//        case cmd => false
//      },
//      version in Docker := "latest",
//      dockerExposedPorts := Seq(8080),
//      maintainer in Docker := "mastering-akka@packt.com",
//      dockerBaseImage := "java:8"
//    )
//  ).dependsOn(mainFile)
//    .enablePlugins(JavaAppPackaging)
//}