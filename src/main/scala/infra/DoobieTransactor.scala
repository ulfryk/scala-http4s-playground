package infra

import cats.*
import cats.effect.*
import cats.effect.std.Console
import config.Config
import doobie.LogHandler
import doobie.util.log.LogEvent
import doobie.util.transactor.Transactor

def getDoobieTransactor[F[_] : Async : Console](conf: Config) =
  Transactor.fromDriverManager[F](
    driver = "org.postgresql.Driver",
    url = s"jdbc:postgresql://${conf.host}:${conf.port}/${conf.database}",
    user = conf.username,
    password = conf.password,
    logHandler = Some { logEvent => Console[F].println(logEvent) }
  )
