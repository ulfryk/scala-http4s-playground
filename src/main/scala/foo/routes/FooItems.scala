package foo.routes

import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import foo.dto.FooItemApiId
import foo.model.{FooItemId, FooItemType}
import org.http4s.*
import org.http4s.dsl.io.*

import scala.util.Try


implicit val typeQueryParamDecoder: QueryParamDecoder[FooItemType] =
  QueryParamDecoder[String]
    .emap(i => Try(FooItemType.valueOf(i))
      .toEither
      .left.map(t => ParseFailure(t.getMessage, t.getMessage)))

object TypeQueryParamMatcher extends OptionalMultiQueryParamDecoderMatcher[FooItemType]("type")

val allIds = List(54321, 54322, 6543212, 123413, 1115)

val fooItemsRoutes = HttpRoutes.of[IO] {
  case GET -> Root / "foo-items" :? TypeQueryParamMatcher(itemType) =>
    val ids = allIds.map(FooItemId(_).toApiString)
    itemType match
      case Invalid(e) => BadRequest(e.foldLeft("")((acc, next) => acc + next))
      case Valid(a) => a match
        case Nil => Ok(s"Hello $ids")
        case list => Ok(s"Hello $ids ($list)")

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    id match
      case Valid(idVal) =>
        if allIds.contains(idVal) then Ok(s"whatever ${idVal.toApiString}")
        else NotFound(s"couldn't find ${idVal.toApiString}")
      case Invalid(es) => BadRequest {
        es.foldLeft("")((txt, next) => txt ++ next.message)
      }


}
