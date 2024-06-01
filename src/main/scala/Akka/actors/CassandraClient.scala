package Akka.actors

import com.datastax.oss.driver.api.core.CqlSession
import java.net.InetSocketAddress

object CassandraClient {
  val session: CqlSession = CqlSession.builder()
    .addContactPoint(new InetSocketAddress("localhost", 9042))
    .withKeyspace("github_data")
    .withLocalDatacenter("datacenter1")
    .build()

  def closeSession(): Unit = session.close()
}
