package infra

import cats.effect.Async
import cats.effect.std.Console
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.middleware.Logger

def httpAppLogged[F[_] : Async : Console](app: HttpRoutes[F]): HttpApp[F] =
  Logger.httpRoutes[F](
    logHeaders = true,
    logBody = true,
    logAction = Some((msg: String) => Console[F].println(msg))
  )(app).orNotFound
