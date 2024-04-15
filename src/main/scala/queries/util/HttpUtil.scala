package queries.util

import caliban.client.AppParameters.githubOauthToken
import caliban.client.CalibanClientError
import org.slf4j.{Logger, LoggerFactory}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.model.Header
import zio._

object HttpUtil {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

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
}
