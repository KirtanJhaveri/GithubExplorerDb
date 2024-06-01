package Akka.actors

import caliban.client.TypeAliases.RepoInfoList
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.{Logger, LoggerFactory}
import queries.RepoQuery
import zio.{Runtime, Unsafe}
import scala.concurrent.ExecutionContext

object RepoActor {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  // Define messages
  sealed trait Message
  case class MessageReceived(message: String, replyTo: ActorRef[RootActor.Message]) extends Message // Include reference to RootActor

  def apply(): Behavior[Message] = Behaviors.receive { (context, message) =>
    implicit val ec: ExecutionContext = context.executionContext // Provide ExecutionContext from ActorContext
    message match {
      case MessageReceived(msg, replyTo) =>
        println(s"Received message: $msg")
        logger.info("RepoActor spawned")
        val runtime = Runtime.default
        Unsafe.unsafe { implicit unsafe =>
          val queryEffect = RepoQuery.run
          val future = runtime.unsafe.runToFuture(queryEffect)
          future.onComplete {
            case scala.util.Success(value) =>
              // Handle the successful result
              val reply: Option[List[Option[Option[Option[RepoInfoList]]]]] = value
              logger.info(s"Repo Actor: ${reply.toString}")
              replyTo ! RootActor.RepoActorReply(reply) // Send reply to RootActor
            case scala.util.Failure(exception) =>
              // Handle the failure (e.g., log error)
              logger.error("An error occurred:", exception)
          }

        }
        Behaviors.same
    }
  }
}