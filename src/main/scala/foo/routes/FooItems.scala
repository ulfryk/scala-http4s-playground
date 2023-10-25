package foo.routes

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.effect.IO
import cats.implicits.*
import foo.dto.{FooItemApiId, FooItemsFilter, NameQueryParam, TypeQueryParam}
import foo.model.FooItemId
import org.http4s.*
import org.http4s.dsl.io.*

private val allIds = List(54321, 54322, 6543212, 123413, 1115)
private val ids = allIds.map(FooItemId(_).toApiString)

private def listIt(filter: Validated[NonEmptyList[ParseFailure], FooItemsFilter]) =
  filter match
    case Invalid(e) => BadRequest(e.foldLeft("")((acc, next) => acc + next))
    case Valid(a) => Ok(s"Hello $ids (${a})")

val fooItemsRoutes = HttpRoutes.of[IO] {
  case GET -> Root / "foo-items-xx" :? FooItemsFilter.Matcher(filter) => listIt(filter)
  case GET -> Root / "foo-items-yy" :? FooItemsFilter.Matcher2(filter) => listIt(filter)
  case GET -> Root / "foo-items" :? TypeQueryParam.Matcher(itemType) +& NameQueryParam.Matcher(itemName) =>
    listIt(FooItemsFilter(itemName, itemType))

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    id match
      case Valid(idVal) =>
        if allIds.contains(idVal) then Ok(s"whatever ${idVal.toApiString}")
        else NotFound(s"couldn't find ${idVal.toApiString}")
      case Invalid(es) => BadRequest {
        es.foldLeft("")((txt, next) => txt ++ next.message)
      }


}
