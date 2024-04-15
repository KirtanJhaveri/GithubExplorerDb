package queries

import caliban.client.CalibanClientError
import caliban.client.Github.IssueTimelineItemsItemType.REFERENCED_EVENT
import caliban.client.Github._
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.model.Header
import zio.Console.printLine
import zio._

class IssueQuery_copy extends ZIOAppDefault {
  var ownername = ""
  var reponame = ""
  val logger: Logger = LoggerFactory.getLogger(getClass)
  def setOwnername(ownername:String): Unit = {
    this.ownername = ownername
    println(s"$ownername when setting owner")
  }
  def setreponame(reponame:String): Unit = {
    println(s"$reponame when setting")
    this.reponame = reponame

  }
  def run: ZIO[Any, Throwable, Unit] = {
    val githubGraphqlEndpoint = uri"https://api.github.com/graphql"
    val githubOauthToken: String = "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"
    println(s"from run $ownername,$reponame")
    val config = ConfigFactory.load()
//    val githubGraphqlEndpoint = uri"${config.getString("github.graphqlEndpoint")}"
//    val githubOauthToken: String = config.getString("github.oauthToken")

    // Define the GraphQL query to fetch issues for a specific repository
    val issuesQuery =
      Query.repository(owner = ownername, name = reponame)(
        Repository.issues(first = Some(5))(IssueConnection.nodes(
          Issue.title ~
            Issue.body ~
            Issue.timelineItems(first = Some(5),itemTypes = Some(List(REFERENCED_EVENT)))(IssueTimelineItemsConnection.nodesOption(onReferencedEvent = Some(ReferencedEvent.commit(Commit.oid ~ Commit.message))))
        )
      ))

    def sendRequest[T](req: Request[Either[CalibanClientError, T], Any]): RIO[SttpBackend[Task, ZioStreams with WebSockets], T] =
      ZIO
        .serviceWithZIO[SttpBackend[Task, ZioStreams with WebSockets]] { backend =>
          req.headers(Header("Authorization", s"Bearer $githubOauthToken")).send(backend)
        }
        .mapError { error =>
          // Print the error message or log it
          println(s"Error during request: $error")
          error
        }
        .map(_.body)
        .absolve
    logger.info("Hello from issue")
    val call1 = sendRequest(issuesQuery.toRequest(githubGraphqlEndpoint, useVariables = true)).tap(res => printLine(s"Result: $res"))

    val result: ZIO[Any, Throwable, Unit] =
      call1
        .provideLayer(HttpClientZioBackend.layer())
        .flatMap { response =>
          // Handle response here
          println(s"Response: $response")
          ZIO.unit
        }
//    println("Hello from issue2")

    result
  }
}
