import cats.effect.*
import com.comcast.ip4s.*
import foo.FooItemsService
import foo.routes.fooItemsRoutes
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

object Main extends IOApp:
  private val fooService = FooItemsService()
  private val httpApp = Router("/" -> fooItemsRoutes(fooService)).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    
    
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
