package Akka.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import org.slf4j.{Logger, LoggerFactory}

object Main extends App {
  import RootActor._

  private val system: ActorSystem[Message] = ActorSystem(RootActor(), "GithubExplorer")

  // Sending a message to the RootActor
  system ! RootActor.Start("Hello, User!")
}







