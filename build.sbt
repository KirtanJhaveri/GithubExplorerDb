import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "GithubExplorerDb"
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.31" % Test
)

//libraryDependencies ++= Seq(
//  "dev.zio" %% "zio-test" % "1.0.12" % Test,
//  "dev.zio" %% "zio-test-sbt" % "2.0.22" % Test,
//  "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.9.5" % Test,
//  "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % "3.5.2" % Test,
//  "com.softwaremill.sttp.client3" %% "mock-backend" % "3.3.15" % Test
//)

//Akka
resolvers += "Akka library repository".at("https://repo.akka.io/maven")
val AkkaVersion = "2.9.2"
val AkkaHttpVersion = "10.6.1"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
)
libraryDependencies += "io.spray" %% "spray-json" % "1.3.6"

//conf
libraryDependencies += "com.typesafe" % "config" % "1.4.3"

libraryDependencies += "com.datastax.oss" % "java-driver-core" % "4.17.0"


//Caliban
enablePlugins(CalibanPlugin)

val calibanVersion = "2.5.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test"          % "2.1.0-RC3" % Test,
  "dev.zio" %% "zio-test-sbt"      % "2.1.0-RC3" % Test,
  "dev.zio" %% "zio-test-magnolia" % "2.1.0-RC3" % Test
)

libraryDependencies += "com.esri.geometry" % "esri-geometry-api" % "2.2.4"
libraryDependencies += "org.apache.tinkerpop" % "gremlin-core" % "3.7.0"
libraryDependencies += "org.apache.tinkerpop" % "tinkergraph-gremlin" % "3.7.2"




libraryDependencies ++= Seq(
  "com.github.ghostdogpr"         %% "caliban"                       % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-quick"                 % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-http4s"                % "2.5.3",
//  "com.github.ghostdogpr"         %% "caliban-play"                  % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-akka-http"             % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-zio-http"              % "2.5.1",
  "dev.zio"                       %% "zio"                           % "2.1.0",
  "com.github.ghostdogpr"         %% "caliban-federation"            % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-tapir"                 % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-client"                % "2.5.3",
  "com.github.ghostdogpr"         %% "caliban-tools"                 % "2.5.3",
  "org.http4s"                    %% "http4s-ember-server"           % "0.23.23",
  "org.http4s"                    %% "http4s-dsl"                    % "0.23.23",
  "com.softwaremill.sttp.client3" %% "zio"                           % "3.9.4",
  "dev.zio"                       %% "zio-interop-cats"              % "23.1.0.0",
  "io.circe"                      %% "circe-generic"                 % "0.14.6",
//  "com.typesafe.akka"             %% "akka-actor-typed"              % "2.8.5",
  "com.softwaremill.sttp.tapir"   %% "tapir-jsoniter-scala"          % "1.10.0",
  "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"              % "1.10.0"
)