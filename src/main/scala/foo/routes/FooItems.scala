package foo.routes

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.effect.IO
import cats.implicits.*
import foo.FooItemsService
import foo.dto.{FooItemApiId, FooItemsFilter, NameQueryParam, TypeQueryParam}
import foo.model.FooItemId
import org.http4s.*
import org.http4s.dsl.io.*

private val allIds = List(54321, 54322, 6543212, 123413, 1115)
private val ids = allIds.map(FooItemId(_).toApiString)

private def listIt(filter: Validated[NonEmptyList[ParseFailure], FooItemsFilter])(using service: FooItemsService) =
  filter match
    case Invalid(e) => BadRequest(e.foldLeft("")((acc, next) => acc + next))
    case Valid(a) => Ok {
      val r = service.getAll(a)
      s"FooItemList :: ids: ${r._2.map(_.id)}\n"
        ++ "---------------------------------------------------------------------------------------------\n"
        ++ r._2.foldLeft("") { (acc, next) => acc ++ s"${next}\n" }
        ++ "---------------------------------------------------------------------------------------------\n"
        ++ s"filter: ${filter}"
    }


def fooItemsRoutes(service: FooItemsService) = HttpRoutes.of[IO] {
  case GET -> Root / "foo-items-xx" :? FooItemsFilter.Matcher(filter) => listIt(filter)(using service)
  case GET -> Root / "foo-items-yy" :? FooItemsFilter.Matcher2(filter) => listIt(filter)(using service)
  case GET -> Root / "foo-items" :? TypeQueryParam.Matcher(itemType) +& NameQueryParam.Matcher(itemName) =>
    listIt(FooItemsFilter(itemName, itemType))(using service)

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    id match
      case Valid(idVal) =>
        if allIds.contains(idVal) then Ok(s"whatever ${idVal.toApiString}")
        else NotFound(s"couldn't find ${idVal.toApiString}")
      case Invalid(es) => BadRequest {
        es.foldLeft("")((txt, next) => txt ++ next.message)
      }


}
