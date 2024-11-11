package foo.routes

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.effect.IO
import cats.implicits.*
import foo.FooItemsService
import foo.dto.{FooItemApiId, FooItemsFilter, NameQueryParam, TypeQueryParam}
import foo.model.{FooItem, FooItemId}
import org.http4s.*
import org.http4s.dsl.io.*

private def listIt(
  filter: Validated[NonEmptyList[ParseFailure], FooItemsFilter],
  service: FooItemsService[IO],
): IO[Response[IO]] =
  filter match
    case Invalid(e) => BadRequest(e.foldLeft("")((acc, next) => acc + next))
    case Valid(a) =>
      for
        r <- service.getAll(a)
        resp <- Ok(listRespText(r._1, r._2))
      yield resp

private def listRespText(filter: FooItemsFilter, items: List[FooItem]): String =
  s"FooItemList :: ids: ${items.map(_.id)}\n"
    ++ "---------------------------------------------------------------------------------------------\n"
    ++ items.foldLeft("") { (acc, next) => acc ++ s"${next.id.toApiString} $next\n" }
    ++ "---------------------------------------------------------------------------------------------\n"
  ++
  s"filter: $filter"

def fooItemsRoutes(service: FooItemsService[IO]) = HttpRoutes.of[IO] {
  case GET -> Root / "foo-items" :? TypeQueryParam.Matcher(itemType) +& NameQueryParam.Matcher(itemName) =>
    listIt(FooItemsFilter(itemName, itemType), service)

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    id match
      case Valid(idVal) =>
        for
          found <- service.getOne(idVal)
          resp <- found match
            case Some(item) => Ok(listRespText(FooItemsFilter.empty, List(item)))
            case None => NotFound(s"couldn't find ${idVal.toApiString}")
        yield resp
      case Invalid(es) => BadRequest {
        es.foldLeft("")((txt, next) => txt ++ next.message)
      }


}
