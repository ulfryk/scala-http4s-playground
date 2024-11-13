import _root_.io.circe.*
import _root_.io.circe.generic.auto.*
import _root_.io.circe.syntax.*
import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import com.comcast.ip4s.*
import common.api.dto.ErrorResponse
import common.api.dto.ErrorResponse.given
import common.model.Kaboom.{ApiKaboom, InternalKaboom}
import common.model.{ErrorCode, ErrorIssue, ErrorIssueLocation, Kaboom}
import config.Config
import doobie.LogHandler
import doobie.util.log.LogEvent
import doobie.util.transactor.Transactor
import foo.api.dto.MalformedFilter
import foo.api.fooItemsRoutes
import foo.dao.doobie.FooRepoDoobie
import foo.dao.skunk.FooRepoSkunk
import foo.domain.FooItemsService
import fs2.io.net.Network
import natchez.Trace
import natchez.Trace.Implicits.noop
import org.http4s.*
import org.http4s.Header.*
import org.http4s.Status.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import skunk.*

object Main extends IOApp:
  private def httpAppLogged(service: FooItemsService[IO]): HttpApp[IO] =
    Logger.httpRoutes[IO](
      logHeaders = true,
      logBody = true,
      logAction = Some((msg: String) => Console[IO].println(msg))
    )(Router("/" -> fooItemsRoutes(service))).orNotFound

  private def getSkunkSession[F[_] : Temporal : Trace : Network : Console](conf: Config) =
    Session.single(
      host = conf.host,
      port = conf.port,
      user = conf.username,
      database = conf.database,
      password = Some(conf.password),
      debug = true,
    )

  private def getDoobieTransactor[F[_] : Async : Console](conf: Config) =
    Transactor.fromDriverManager[F](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql://${conf.host}:${conf.port}/${conf.database}",
      user = conf.username,
      password = conf.password,
      logHandler = Some { logEvent => Console[F].println(logEvent) }
    )

  private def unifyError(err: Throwable): Kaboom = err match
    case err: Kaboom => err
    case err @ MalformedFilter(errs) => ApiKaboom(
      message = err.getMessage,
      code = ErrorCode.Invalid,
      issues = errs.map(w => ErrorIssue(ErrorIssueLocation.QueryParams, "query", w.sanitized)).some,
      cause = err.some,
    )
    case x => InternalKaboom(
      message = "Something went wrong.",
      cause = x.some,
    )

  private def startServer(service: FooItemsService[IO]) =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8111")
      .withHttpApp(httpAppLogged(service))
      .withErrorHandler { err =>
        unifyError(err) match
          case e: ApiKaboom =>
            val resp = ErrorResponse(e).asJson
            e.code match
              case ErrorCode.Invalid => BadRequest(resp)
              case ErrorCode.Conflict => Conflict(resp)
              case ErrorCode.Internal => InternalServerError(resp)
              case ErrorCode.NotFound => NotFound(resp)
          case e: InternalKaboom => InternalServerError(ErrorResponse(e).asJson)
      }
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
    val service = FooItemsService(repo)
    startServer(service)

  def run(args: List[String]): IO[ExitCode] =
    ConfigSource.default.at("db").load[Config] match
      case Left(e) => IO.println(e.toString).as(ExitCode.Error)
      case Right(c) => runWithDoobie(c).as(ExitCode.Success)
