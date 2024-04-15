package Akka.actor

import Akka.actor.RootActor.getClass
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import caliban.client.TypeAliases.IssueInfoList
import org.slf4j.{Logger, LoggerFactory}
import queries.{IssueQuery, IssueQuery_copy}
import zio.{Runtime, Unsafe}

object IssueFetchActor {
  sealed trait Command
  case class FetchIssues(owner: String, repoName: String, replyTo: ActorRef[RootActor.Message]) extends Command

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    implicit val ec = context.executionContext // Use the actor's ExecutionContext for handling Futures

    Behaviors.receiveMessage {
      case FetchIssues(repoName, ownerName, replyTo) =>
        logger.info(s"Fetching issues for $ownerName/$repoName")
        val runtime = Runtime.default

        Unsafe.unsafe { implicit unsafe =>
          // Running the ZIO effect and converting it to a Future
          val obj = new IssueQuery_copy()
          obj.setreponame(repoName)
          obj.setOwnername(ownerName)
//          logger.info(s" before call $ownerName/$repoName")
//          logger.info(s" before call from issue query ${obj.ownername}/${obj.reponame}")
          val future = runtime.unsafe.runToFuture(obj.run)
//          logger.info(s" after call $ownerName/$repoName")
          future.onComplete {
            case scala.util.Success(value) =>
              val reply: Option[List[Option[Option[Option[IssueInfoList]]]]] = value
              logger.info(s"Issues successfully fetched for $repoName: ${reply.toString}")
              replyTo ! RootActor.IssueFetchActorReply(value.toString)
            case scala.util.Failure(exception) =>
              logger.error(s"Failed to fetch issues for $repoName: ${exception.getMessage}")
          }
        }

        Behaviors.same
    }
  }
}




//object IssueFetchActor {
//  sealed trait Command
//  case class FetchIssues(owner: String, repoName: String, replyTo: ActorRef[RootActor.Message]) extends Command
//
//  private val logger: Logger = LoggerFactory.getLogger(getClass)
//
//  def apply(): Behavior[Command] = Behaviors.setup { context =>
//    implicit val ec = context.executionContext // Use Actor's ExecutionContext for Futures
//
//    Behaviors.receiveMessage {
//      case FetchIssues(owner, repoName, replyTo) =>
//        logger.info(s"Fetching issues for $owner/$repoName")
//        val runtime = Runtime.default
//        Unsafe.unsafe { implicit unsafe =>
//          val queryEffect = IssueQuery.run // No reply mapping needed
//          val future = runtime.unsafe.runToFuture(queryEffect)
//          future.onComplete {
//            case scala.util.Success(value) =>
//              logger.info(s"starting $repoName")
//              // Handle the successful result
//              //            val reply: Option[List[Option[Option[Option[RepoInfoList]]]]] = value
//              logger.info(value.toString)
//            //            replyTo ! RootActor.RepoActorReply(value) // Send reply to RootActor
//            case scala.util.Failure(exception) =>
//              // Handle the failure (e.g., log error)
//              logger.error("An error occurred:", exception)
//          }
//          //      runtime.unsafeRunAsync_(IssueQuery.run(owner, repoName)) // Adapt the IssueQuery to accept parameters
//          Behaviors.same
//        }
//    }
//  }
//}


