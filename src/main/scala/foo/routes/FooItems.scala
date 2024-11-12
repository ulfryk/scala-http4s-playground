package foo.routes

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.effect.IO
import cats.implicits.*
import common.api.headers.{`Pagination-Applied-Filter`, `Pagination-Total-Count`}
import foo.FooItemsService
import foo.dto.*
import foo.dto.FooItemsFilterApi.given
import foo.model.*
import fs2.*
import org.http4s.*
import org.http4s.Header.*
import org.http4s.dsl.io.*

import java.nio.charset.StandardCharsets
import scala.util.Try

private def listIt(
  filter: Validated[NonEmptyList[ParseFailure], FooItemsFilter],
  service: FooItemsService[IO],
): IO[Response[IO]] =
  filter match
    case Invalid(e) => BadRequest(e.foldLeft("")((acc, next) => acc + next))
    case Valid(a) =>
      for
        r <- service.getAll(a)
        resp <- Ok(listRespText(r._1, r._2)).map { x =>
          x.copy(headers = x.headers ++ `Pagination-Total-Count`(r._2.size) ++ `Pagination-Applied-Filter`(r._1))
        }
      yield resp

private def listRespText(filter: FooItemsFilter, items: List[FooItem]): String =
  s"FooItemList:\n"
    ++ "---------------------------------------------------------------------------------------------\n"
    ++ items.foldLeft("") { (acc, next) => acc ++ s"${next.id.toApiString} $next\n" }
    ++ "---------------------------------------------------------------------------------------------\n"
  ++
  s"filter: $filter"

private def bodyFromText(body: String): Either[String, NewFooItem] =
  if (body.contains("\n")) Left("multiline")
  else if (body.isBlank) Left("empty")
  else body.split(",").toList match
    case _ :: _ :: _ :: _ :: _ => Left("too many tokens")
    case fooName :: fooText :: fooType :: Nil =>
      Try(FooItemType.valueOf(fooType))
        .toEither.leftMap(_.getMessage)
        .map(NewFooItem(FooItemName(fooName), FooItemText(fooText), _))
    case _ :: _ :: Nil | _ :: Nil => Left("too few tokens")
    case Nil => Left("empty")

def fooItemsRoutes(service: FooItemsService[IO]) = HttpRoutes.of[IO] {
  case req@POST -> Root / "foo-items" =>
    for
      body <- req.body.compile.toList.map {
        bytes => new String(bytes.toArray, StandardCharsets.UTF_8)
      }
      resp <- bodyFromText(body) match
        case Left(err) => BadRequest(err)
        case Right(inp) => service.create(inp)
          .flatMap(c => Ok(listRespText(FooItemsFilter.empty, List(c))))
    yield resp

  case GET -> Root / "foo-items"
    :? TypeQueryParam.Matcher(itemType)
    +& NameQueryParam.Matcher(itemName)
    +& TextQueryParam.Matcher(itemText) =>
    listIt(FooItemsFilterApi(itemName, itemText, itemType), service)

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
