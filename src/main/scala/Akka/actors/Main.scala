package Akka.actors

import akka.actor.typed.ActorSystem

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  import RootActor._

  private val system: ActorSystem[Message] = ActorSystem(RootActor(), "GithubExplorer")

  sys.addShutdownHook {
    println("Shutdown hook is being executed. Closing resources.")
    CassandraClient.closeSession()
    system.terminate()
  }

  // Sending a message to the RootActor
  system ! RootActor.Start("Hello, User!")

  // Explicitly handle ActorSystem termination
  system.whenTerminated.foreach { _ =>
    println("Actor System has terminated. Exiting application.")
    System.exit(0)  // Ensure the JVM halts even if non-daemon threads are running
  }
}

//object Main extends App {
//  import RootActor._
//
//  private val system: ActorSystem[Message] = ActorSystem(RootActor(), "GithubExplorer")
//  sys.addShutdownHook {
//    CassandraClient.closeSession()
//    system.terminate()
//  }
//  // Sending a message to the RootActor
//  system ! RootActor.Start("Hello, User!")
//}