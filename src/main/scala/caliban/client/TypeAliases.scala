// TypeAliases.scala
package caliban.client

import caliban.client.Github.URI
import com.typesafe.config.Config
import sttp.client3.UriContext

// Define type aliases for improved readability
object TypeAliases {
  type RepoInfoList = (String,String, Option[Int], Boolean, Boolean, Boolean, Boolean, Option[String], URI)
  type IssueInfoList = (String, String, Option[List[Option[Option[Option[(String,String)]]]]])
}

object AppParameters{
  import com.typesafe.config.ConfigFactory
  //Load and read variables from configuration file
  val config: Config = ConfigFactory.load()
  val githubGraphqlEndpoint = uri"${config.getString("Github.graphqlEndpoint")}"
  val githubOauthToken: String = config.getString("Github.oauthToken")
  val searchLanguage: String = config.getString("Github.search.language")
  val searchFirst: Int = config.getInt("Github.search.first")
}
