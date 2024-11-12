package common.api.headers

import cats.*
import cats.syntax.all.*
import org.http4s.util.{Renderable, Writer}
import org.http4s.{Header, Headers, ParseFailure}
import org.typelevel.ci.*

final case class `Pagination-Total-Count`(count: Long)

object `Pagination-Total-Count` {
  
  def apply(count: Long): Headers =
    Headers(new `Pagination-Total-Count`(count))
    
  given headerInstance: Header[`Pagination-Total-Count`, Header.Single] =
    Header.createRendered(
      ci"Pagination-Total-Count",
      h =>
        new Renderable {
          def render(writer: Writer): writer.type = writer.append(h.count.toString)
        },

      h =>
        try Right(new `Pagination-Total-Count`(h.toLong))
        catch case _: NumberFormatException => Left(ParseFailure(h, "is not a valid integer"))
    )
}
