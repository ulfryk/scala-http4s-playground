import cats.effect.*
import cats.effect.std.Console
import com.comcast.ip4s.*
import foo.FooItemsService
import foo.routes.fooItemsRoutes
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

object Main extends IOApp:
  private val fooService = FooItemsService()
  private val httpApp: HttpRoutes[IO] = Router("/" -> fooItemsRoutes(fooService))
  private val httpAppLogged: HttpApp[IO] = Logger.httpRoutes[IO](
    logHeaders = true,
    logBody = true,
    logAction = Some((msg: String) => Console[IO].println(msg))
  )(httpApp).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8111")
      .withHttpApp(httpAppLogged)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
