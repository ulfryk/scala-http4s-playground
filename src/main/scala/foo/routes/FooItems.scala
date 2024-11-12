package foo.routes

import _root_.io.circe.*
import _root_.io.circe.generic.auto.*
import _root_.io.circe.syntax.*
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.effect.*
import cats.implicits.*
import common.api.headers.{`Pagination-Applied-Filter`, `Pagination-Total-Count`}
import foo.FooItemsService
import foo.dto.*
import foo.dto.CircleCodecs.given
import foo.dto.FooItemApiId.toApiString
import foo.dto.FooItemsFilterApi.given
import foo.model.*
import org.http4s.*
import org.http4s.Header.*
import org.http4s.circe.*
import org.http4s.dsl.io.*


def fooItemsRoutes(service: FooItemsService[IO]) = HttpRoutes.of[IO] {
  case req @ POST -> Root / "foo-items" =>
    for
      input <- req.as[NewFooItem]
      created <- service.create(input)
      resp <- Ok(created.asJson)
    yield resp

  case GET -> Root / "foo-items"
    :? TypeQueryParam.Matcher(itemType)
    +& NameQueryParam.Matcher(itemName)
    +& TextQueryParam.Matcher(itemText) =>
    FooItemsFilterApi(itemName, itemText, itemType) match
      case Invalid(e) => BadRequest(e.foldLeft("")(_ + _))
      case Valid(filter) =>
        for
          data <- service.getAll(filter)
          resp <- Ok(data.asJson).map { x =>
            x.copy(headers = x.headers
              ++ `Pagination-Total-Count`(data.size)
              ++ `Pagination-Applied-Filter`(filter))
          }
        yield resp

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    id match
      case Valid(idVal) =>
        for
          found <- service.getOne(idVal)
          resp <- found match
            case Some(item) => Ok(item.asJson)
            case None => NotFound(s"couldn't find ${idVal.toApiString}")
        yield resp
      case Invalid(es) => BadRequest {
        es.foldLeft("")(_ ++ _.message)
      }

}
