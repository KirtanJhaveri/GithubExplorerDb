package queries

import caliban.client.Operations.RootQuery
import caliban.client.{CalibanClientError, SelectionBuilder}
import caliban.client.Github.SearchType.REPOSITORY
import caliban.client.Github._
import org.slf4j.{Logger, LoggerFactory}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.model.Header
import zio.Console.printLine
import zio._
import com.typesafe.config.ConfigFactory
import caliban.client.TypeAliases.RepoInfoList


object RepoQuery extends ZIOAppDefault {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def run: ZIO[Any, Throwable, Option[List[Option[Option[Option[RepoInfoList]]]]]] = {

    //Load and read variables from configuration file
    val config = ConfigFactory.load()
    val githubGraphqlEndpoint = uri"${config.getString("Github.graphqlEndpoint")}"
    val githubOauthToken: String = config.getString("Github.oauthToken")
    val searchLanguage: String = config.getString("Github.search.language")
    val searchFirst: Int = config.getInt("Github.search.first")

    //Combining the fields to be sent in the query
    val repository: SelectionBuilder[Repository, RepoInfoList] = Repository.name ~ Repository.ownerInterface(owner = RepositoryOwner.login) ~
      Repository.diskUsage ~ Repository.hasIssuesEnabled ~ Repository.isArchived ~ Repository.isDisabled ~ Repository.isEmpty ~
      Repository.primaryLanguage(Language.name) ~ Repository.url

    //Sending the query
    val Rquery: SelectionBuilder[RootQuery, Option[List[Option[Option[Option[RepoInfoList]]]]]] =
      Query.
        search(first = Some(searchFirst), query = s"language:$searchLanguage", `type` = REPOSITORY)(
          SearchResultItemConnection.
            edges(SearchResultItemEdge.
              nodeOption(onRepository = Some(repository)
              )
            )
        )

    def sendRequest[T](req: Request[Either[CalibanClientError, T], Any]): RIO[SttpBackend[Task, ZioStreams with WebSockets], T] =
      ZIO
        .serviceWithZIO[SttpBackend[Task, ZioStreams with WebSockets]] { backend =>
          req.headers(Header("Authorization", s"Bearer $githubOauthToken")).send(backend)
        }
        .mapError { error =>
          logger.error(s"Error during request: $error")
          error
        }
        .map(_.body)
        .absolve

    val call1 = sendRequest(Rquery.toRequest(githubGraphqlEndpoint, useVariables = true))

    val result: ZIO[Any, Throwable, Option[List[Option[Option[Option[RepoInfoList]]]]]] =
      call1
        .provideLayer(HttpClientZioBackend.layer())
//        .map(_.toString)
        .tap(response => printLine(s"Response: $response"))

    result
  }
}