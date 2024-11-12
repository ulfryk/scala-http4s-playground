import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import com.comcast.ip4s.*
import config.Config
import foo.FooItemsService
import foo.dao.FooRepoSkunk
import foo.routes.fooItemsRoutes
import fs2.io.net.Network
import natchez.Trace
import natchez.Trace.Implicits.noop
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import pureconfig.ConfigSource
import skunk.*
//import doobie.util.transactor.Transactor
//import doobie.util.transactor.Transactor.Aux

object Main extends IOApp:
  private def httpAppLogged(service: FooItemsService[IO]): HttpApp[IO] = Logger.httpRoutes[IO](
    logHeaders = true,
    logBody = true,
    logAction = Some((msg: String) => Console[IO].println(msg))
  )(Router("/" -> fooItemsRoutes(service))).orNotFound

  private def getSession[F[_] : Temporal : Trace : Network : Console](conf: Config) = Session.single(
    host = conf.host,
    port = conf.port,
    user = conf.username,
    database = conf.database,
    password = Some(conf.password),
    debug = true,
  )

//  private def getDoobieTransactor(conf: Config): Aux[IO, Unit] =
//    Transactor.fromDriverManager[IO](
//      driver = "org.postgresql.Driver",
//      url = s"jdbc:postgresql://${conf.host}:${conf.port}/${conf.database}",
//      user = conf.username,
//      password = conf.password,
//      logHandler = None // Don't setup logging for now. See Logging page for how to log events in detail
//    )

  private def startServer(service: FooItemsService[IO]): IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8111")
      .withHttpApp(httpAppLogged(service))
      .build
      .useForever

  def run(args: List[String]): IO[ExitCode] =
    ConfigSource.default.at("db").load[Config] match
      case Left(e) => IO.println(e.toString).as(ExitCode.Error)
      case Right(c) => getSession[IO](c)
        .use { session =>
          FooRepoSkunk(session).flatMap(r => startServer(FooItemsService(r)))
        }
        .as(ExitCode.Success)
