package Akka.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import caliban.client.TypeAliases.IssueInfoList
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import org.slf4j.{Logger, LoggerFactory}
import queries.IssueQuery
import zio.{Runtime, Unsafe}

import scala.concurrent.ExecutionContextExecutor

object IssueFetchActor {
  sealed trait Command
  case class FetchIssues(owner: String, repoName: String, replyTo: ActorRef[RootActor.Message]) extends Command

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    implicit val ec: ExecutionContextExecutor = context.executionContext // Use the actor's ExecutionContext for handling Futures

    Behaviors.receiveMessage {
      case FetchIssues(repoName, ownerName, replyTo) =>
        logger.info(s"Issue Actor spawned for $ownerName/$repoName")
        val runtime = Runtime.default

        Unsafe.unsafe { implicit unsafe =>
          // Running the ZIO effect and converting it to a Future
          val obj = new IssueQuery()
          obj.setRepoName(repoName)
          obj.setOwnerName(ownerName)
          //          logger.info(s" before call $ownerName/$repoName")
          //          logger.info(s" before call from issue query ${obj.ownerName}/${obj.repoName}")
          val future = runtime.unsafe.runToFuture(obj.run)
          //          logger.info(s" after call $ownerName/$repoName")
          future.onComplete {
            case scala.util.Success(value) =>
//              val issuesFormatted = formatIssues(value)
//              val reply: Option[Option[List[Option[IssueInfoList]]]] = value
              val fin = formatIssues2(repoName,ownerName,value)
              logger.info(s"Issues successfully fetched for $repoName:\n$fin")
              //              logger.info(s"Issues successfully fetched for $repoName: ${reply.toString}")
              logger.info("Issue Done")
              replyTo ! RootActor.IssueFetchActorReply(value)
            case scala.util.Failure(exception) =>
              logger.error(s"Failed to fetch issues for $repoName: ${exception.getMessage}")
          }
        }

        Behaviors.same
    }
  }

//
  private def formatIssues2(repoName:String, ownerName:String, issues: Option[Option[List[Option[IssueInfoList]]]]): String = {
    issues match {
      case Some(Some(issueList)) =>
        if (issueList.flatten.isEmpty) "No issues found"
        else {
          val formattedIssues = issueList.flatten.map {
            case (id,title, body, nestedData) =>
              val insertStmt = SimpleStatement.builder("INSERT INTO github_data.issues (owner_name, repo_name, issue_id, title, body) VALUES (?, ?, ?, ?, ?)")
                .addPositionalValues(repoName,ownerName,id,title,body)
                .build()
              CassandraClient.session.execute(insertStmt)

              val formattedNestedData = nestedData match {
                case Some(list) =>
                  list.flatten.flatten.flatten.map {
                    case (commitSha, commitMessage) =>

                      val insertStmt = SimpleStatement.builder("INSERT INTO github_data.commits (owner_name, repo_name, issue_id, commit_id, commit_message) VALUES (?, ?, ?, ?, ?)")
                        .addPositionalValues(repoName,ownerName,id,commitSha,commitMessage)
                        .build()
                      CassandraClient.session.execute(insertStmt)
                      s"Commit SHA: $commitSha, Message: $commitMessage"
                  }.mkString("\n")
                case None => "No associated commits"
              }
              s"Issue Title: $title\nIssue Body: $body\nAssociated Commits:\n$formattedNestedData"
          }
          formattedIssues.mkString("\n\n")
        }
      case _ => "No issues found"
    }
  }
//  private def formatIssues(issues: Option[Option[List[Option[IssueInfoList]]]]): String = {
    //    issues match {
    //      case Some(Some(issueList)) =>
    //        issueList.flatten match {
    //          case Nil => "No issues found"
    //          case listOfIssues =>
    //            listOfIssues.map(formatIssueInfo).mkString("\n\n")
    //        }
    //      case _ => "No issues found"
    //    }
    //  }
    //
    //  private def formatIssueInfo(issueInfo: IssueInfoList): String = {
    //    val (id,title, body, nestedData) = issueInfo
    //    val formattedNestedData = nestedData match {
    //      case Some(list) =>
    //        list.flatten.flatten.flatten.map {
    //          case (commitSha, commitMessage) => s"Commit SHA: $commitSha, Message: $commitMessage"
    //        }.mkString("\n")
    //      case None => "No associated commits"
    //    }
    //    s"Issue id: $id Issue Title: $title\nIssue Body: $body\nAssociated Commits:\n$formattedNestedData"
    //  }



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


