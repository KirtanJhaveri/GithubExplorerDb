// TypeAliases.scala
package caliban.client

import caliban.client.Github.URI

// Define type aliases for improved readability
object TypeAliases {
  type RepoInfoList = (String,String, Option[Int], Boolean, Boolean, Boolean, Boolean, Option[String], URI)
  type IssueInfoList = (String, String, Option[List[Option[Option[Option[(String,String)]]]]])
}
