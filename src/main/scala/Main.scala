import cats.*
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import common.api.dto.ErrorResponse
import config.Config
import foo.api.error.unifyError
import foo.api.fooItemsRoutes
import foo.dao.doobie.FooRepoDoobie
import foo.dao.skunk.FooRepoSkunk
import foo.domain.FooItemsService
import fs2.io.net.Network
import infra.{getDoobieTransactor, getSkunkSession, httpAppLogged}
import natchez.Trace
import natchez.Trace.Implicits.noop
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import pureconfig.ConfigSource

object Main extends IOApp:

  private def startServer(service: FooItemsService[IO]) =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8111")
      .withHttpApp(httpAppLogged(Router("/" -> fooItemsRoutes(service))))
      .withErrorHandler(err => ErrorResponse.toResponse(unifyError(err)))
      .build
      .useForever

  private def runWithSkunk(conf: Config) =
    getSkunkSession[IO](conf).use { session =>
      val repo = FooRepoSkunk(session)
      val service = FooItemsService(repo)
      startServer(service)
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
