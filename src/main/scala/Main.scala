import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import com.comcast.ip4s.*
import config.Config
import doobie.LogHandler
import doobie.util.log.LogEvent
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import foo.FooItemsService
import foo.dao.{FooRepoDoobie, FooRepoSkunk}
import foo.routes.fooItemsRoutes
import fs2.io.net.Network
import natchez.Trace
import natchez.Trace.Implicits.noop
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import skunk.*

object Main extends IOApp:
  private def httpAppLogged(service: FooItemsService[IO]): HttpApp[IO] = Logger.httpRoutes[IO](
    logHeaders = true,
    logBody = true,
    logAction = Some((msg: String) => Console[IO].println(msg))
  )(Router("/" -> fooItemsRoutes(service))).orNotFound

  private def getSkunkSession[F[_] : Temporal : Trace : Network : Console](conf: Config) = Session.single(
    host = conf.host,
    port = conf.port,
    user = conf.username,
    database = conf.database,
    password = Some(conf.password),
    debug = true,
  )

  private def printLogHandler[F[_] : Console]: LogHandler[F] = (logEvent: LogEvent) => Console[F].println(logEvent)

  private def getDoobieTransactor[F[_] : Async : Console](conf: Config): Aux[F, Unit] =
    Transactor.fromDriverManager[F](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql://${conf.host}:${conf.port}/${conf.database}",
      user = conf.username,
      password = conf.password,
      logHandler = Some(printLogHandler) // Don't setup logging for now. See Logging page for how to log events in detail
    )

  private def startServer(service: FooItemsService[IO]): IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8111")
      .withHttpApp(httpAppLogged(service))
      .build
      .useForever

  private def runWithSkunk(conf: Config) =
    getSkunkSession[IO](conf).use { session =>
      FooRepoSkunk(session)
        .flatMap(r => startServer(FooItemsService(r)))
    }

  private def runWithDoobie(config: Config) =
    val transactor = getDoobieTransactor[IO](config)
    val repo = FooRepoDoobie(transactor)
    startServer(FooItemsService(repo))

  def run(args: List[String]): IO[ExitCode] =
    ConfigSource.default.at("db").load[Config] match
      case Left(e) => IO.println(e.toString).as(ExitCode.Error)
      case Right(c) => runWithDoobie(c)
        .as(ExitCode.Success)
