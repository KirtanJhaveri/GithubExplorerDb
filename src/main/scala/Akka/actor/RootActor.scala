package Akka.actor

import caliban.client.TypeAliases.RepoInfoList
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import caliban.client.Github.URI
import org.slf4j.{Logger, LoggerFactory}

object RootActor {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  // Define messages for RootActor
  sealed trait Message
  case class Start(message: String) extends Message
  case class RepoActorReply(reply: Option[List[Option[Option[Option[RepoInfoList]]]]]) extends Message
  case class IssueFetchActorReply(reply: String) extends Message

  def apply(): Behavior[Message] = Behaviors.setup { context =>
    // Track active issue fetch actors
//    var activeIssueFetches = Set.empty[String]
    var activeIssueFetches = List.empty[String]
    Behaviors.receiveMessage {
      case Start(msg) =>
        val repoActor = context.spawn(RepoActor(), "RepoActor")
        repoActor ! RepoActor.MessageReceived(msg, context.self)
        Behaviors.same  // Continue to receive other messages

      case RepoActorReply(reply) =>
        logger.info("Received reply from RepoActor")
        reply.foreach(_.flatten.flatten.flatten.foreach {
          case (repoName,ownerName, _, isIssuesEnabled, _, _, _, _, uri) if isIssuesEnabled =>
            logger.info(s"Repository Name: $repoName, Issues Enabled: $isIssuesEnabled, URI: $uri")
            val issueActor = context.spawn(IssueFetchActor(), s"IssueActor-$repoName")
            issueActor ! IssueFetchActor.FetchIssues(repoName, ownerName.toString, context.self)
            activeIssueFetches :+ repoName
          case (name,ownername, _, isIssuesEnabled, _, _, _, _, uri) if !isIssuesEnabled =>
            logger.info(s"Not $name")  // Ignore if issues are not enabled
        })
        Behaviors.same  // Continue to receive other messages

      case IssueFetchActorReply(reply) =>
        logger.info("Received reply from IssueActor")
        logger.info(s"$reply")
//        activeIssueFetches -= reply  // Assuming 'reply' includes the actor's unique identifier/name

//        println(activeIssueFetches)

        if (activeIssueFetches.isEmpty) {
          Behaviors.stopped  // Stop the actor if all responses are received
        } else {
          activeIssueFetches = activeIssueFetches.tail
          Behaviors.same  // Continue to receive other messages
        }
    }
  }
}


//package Akka.actor
//
//import caliban.client.TypeAliases.RepoInfoList
//import akka.actor.typed.Behavior
//import akka.actor.typed.scaladsl.Behaviors
//import caliban.client.Github.URI
//import org.slf4j.{Logger, LoggerFactory}
//
//object RootActor {
//  private val logger: Logger = LoggerFactory.getLogger(getClass)
//
//  // Define messages for RootActor
//  sealed trait Message
//  case class Start(message: String) extends Message
//  case class RepoActorReply(reply: Option[List[Option[Option[Option[RepoInfoList]]]]]) extends Message
//  case class IssueFetchActorReply(reply: String) extends Message
//  def apply(): Behavior[Message] = Behaviors.receive { (context, message) =>
//    message match {
//      case Start(msg) =>
//        val repoActor = context.spawn(RepoActor(), "RepoActor")
//        repoActor ! RepoActor.MessageReceived(msg, context.self)
//        Behaviors.same
//      case RepoActorReply(reply) =>
//        logger.info("Received reply from RepoActor")
//        reply.foreach(_.flatten.flatten.flatten.foreach { // Unwrapping nested Options
//          case (name, diskUsage, isIssuesEnabled, isArchived, isDisabled, isEmpty, primaryLanguage, uri) if isIssuesEnabled =>
//            logger.info(s"Repository Name: $name, Issues Enabled: $isIssuesEnabled, URI: $uri")
//            val issueActor = context.spawn(IssueFetchActor(), s"IssueActor-$name")
//            issueActor ! IssueFetchActor.FetchIssues("octocat", name, context.self)
//          case (name, _, isIssuesEnabled, _, _, _, _, uri) if !isIssuesEnabled => logger.info(s"Not $name")// Ignore if isIssuesEnabled is false
//        })
//      case IssueFetchActorReply(reply) =>
//        logger.info("Received reply from IssueActor")
//        logger.info(s"$reply")
//
//        Behaviors.stopped
//    }
//  }
//}