package common.api.headers

import org.http4s.*
import org.http4s.Header.*
import org.http4s.util.{Renderable, Writer}
import org.typelevel.ci.*

final case class `Pagination-Applied-Filter`(name: String, value: String)

object `Pagination-Applied-Filter` {
  def apply[A](filters: A)(using serialize: A => Seq[(String, String)]): Headers =
    val raw = serialize(filters).map((n, v) => `Pagination-Applied-Filter`(n, v))
    Headers(raw)

  given headerInstance: Header[`Pagination-Applied-Filter`, Header.Recurring] =
    Header.createRendered(
      ci"Pagination-Applied-Filter",
      h =>
        new Renderable {
          def render(writer: Writer): writer.type = writer.append(s"${h.name}=${h.value}")
        },

      h =>
        h.split('=').toList match
          case n :: v :: Nil => Right(`Pagination-Applied-Filter`(n, v))
          case _ => Left(ParseFailure(h, "is not a valid filter"))
    )
}

 
