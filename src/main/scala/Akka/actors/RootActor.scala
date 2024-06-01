package Akka.actors

import caliban.client.TypeAliases.{IssueInfoList, RepoInfoList}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import org.slf4j.{Logger, LoggerFactory}
import zio.prelude.data.Optional.AllValuesAreNullable


object RootActor {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  // Define messages for RootActor
  sealed trait Message
  case class Start(message: String) extends Message
  case class RepoActorReply(reply: Option[List[Option[Option[Option[RepoInfoList]]]]]) extends Message
  case class IssueFetchActorReply(reply: Option[Option[List[Option[IssueInfoList]]]]) extends Message

  def apply(): Behavior[Message] = Behaviors.setup { context =>
    // Track active issue fetch actors
    //    var activeIssueFetches = Set.empty[String]
    var activeIssueFetches = 0
    Behaviors.receiveMessage {
      case Start(msg) =>
        val repoActor = context.spawn(RepoActor(), "RepoActor")
        repoActor ! RepoActor.MessageReceived(msg, context.self)
        Behaviors.same  // Continue to receive other messages

      case RepoActorReply(reply) =>
        logger.info("Received reply from RepoActor")
        reply.foreach(_.flatten.flatten.flatten.foreach {
          case (repoName,ownerName, diskUsage, isIssuesEnabled,isArchived, isDisabled, isEmpty, primaryLanguage, uri) if isIssuesEnabled =>
            logger.info(s"Repository Name: $repoName, Issues Enabled: $isIssuesEnabled, URI: $uri")
            val insertStmt = SimpleStatement.builder("INSERT INTO github_data.repositories (repo_name, owner_name, disk_usage, has_issues_enabled, is_archived, is_disabled, is_empty, primary_language, repo_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
              .addPositionalValues(
                repoName,
                ownerName,
                diskUsage.getOrElse(null.asInstanceOf[Int]), // Using `asInstanceOf[Int]` to indicate type to Scala's type system
                isIssuesEnabled, // Assuming `false` as default for Boolean
                isArchived,
                isDisabled,
                isEmpty,
                primaryLanguage.orNull, // If `primaryLanguage` is an Option[String]
                uri.trim()
              )
              .build()
            CassandraClient.session.execute(insertStmt)
            val issueActor = context.spawn(IssueFetchActor(), s"IssueActor-$repoName")
            issueActor ! IssueFetchActor.FetchIssues(repoName, ownerName, context.self)
            activeIssueFetches +=1
          case (name,ownerName, _, isIssuesEnabled, _, _, _, _, _) if !isIssuesEnabled =>
            logger.info(s"Issues Not enabled $ownerName/$name")  // Ignore if issues are not enabled
        })
        Behaviors.same  // Continue to receive other messages

      case IssueFetchActorReply(reply) =>
        activeIssueFetches -= 1  // Decrement counter when an IssueFetchActor completes
        if (activeIssueFetches == 0) {
          logger.info("All IssueFetchActors have completed. Stopping RootActor.")
          Behaviors.stopped  // Stop the actor if all responses are received
        } else {
          Behaviors.same
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