package Akka.actors

import queries.RepoQuery
import zio._

object AnotherObject {
  def main(args: Array[String]): Unit = {
    val program = RepoQuery.run
    val runtime = Runtime.default
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(program).getOrThrow()
    }
  }
}

