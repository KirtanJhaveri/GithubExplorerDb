package queries

import caliban.client.AppParameters.{githubGraphqlEndpoint, githubOauthToken}
import caliban.client.Github.IssueTimelineItemsItemType.REFERENCED_EVENT
import caliban.client.Github._
import caliban.client.TypeAliases.IssueInfoList
import caliban.client.{CalibanClientError, Operations, SelectionBuilder}
import org.slf4j.{Logger, LoggerFactory}
import queries.RepoQuery.logger
import queries.util.HttpUtil.sendRequest
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.model.Header
import zio._

class IssueQuery extends ZIOAppDefault {
  var ownerName = ""
  var repoName = ""
  val logger: Logger = LoggerFactory.getLogger(getClass)
  def setOwnerName(ownerName:String): Unit = {
    this.ownerName = ownerName

  }
  def setRepoName(repoName:String): Unit = {

    this.repoName = repoName

  }
  def run: ZIO[Any, Throwable, Option[Option[List[Option[IssueInfoList]]]]] = {

    // Define the GraphQL query to fetch issues for a specific repository
    val issuesQuery:SelectionBuilder[Operations.RootQuery, Option[Option[List[Option[IssueInfoList]]]]] =
      Query.repository(owner = ownerName, name = repoName)(
        Repository.issues(first = Some(5))(IssueConnection.nodes(
          Issue.id~
          Issue.title ~
            Issue.body ~
            Issue.timelineItems(first = Some(5),
              itemTypes = Some(List(REFERENCED_EVENT)))(IssueTimelineItemsConnection.nodesOption(
              onReferencedEvent = Some(ReferencedEvent.commit(Commit.oid ~ Commit.message))))
        )
      ))


    logger.info(s"Running Issue Query for $repoName")
    val call1 = sendRequest(issuesQuery.toRequest(githubGraphqlEndpoint, useVariables = true))

    val result: ZIO[Any, Throwable, Option[Option[List[Option[IssueInfoList]]]]] =
      call1
        .provideLayer(HttpClientZioBackend.layer())
//        .tap(response => printLine(s"Issue Response: $response"))
    //    println("Hello from issue2")

    result
  }
}
