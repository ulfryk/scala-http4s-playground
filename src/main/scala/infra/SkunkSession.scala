package infra

import cats.*
import cats.effect.*
import cats.effect.std.Console
import config.Config
import fs2.io.net.Network
import natchez.Trace
import skunk.*

def getSkunkSession[F[_] : Temporal : Trace : Network : Console](conf: Config) =
  Session.single(
    host = conf.host,
    port = conf.port,
    user = conf.username,
    database = conf.database,
    password = Some(conf.password),
    debug = true,
  )
