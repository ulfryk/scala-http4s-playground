package foo.api

import _root_.io.circe.*
import _root_.io.circe.generic.auto.*
import _root_.io.circe.syntax.*
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}
import cats.effect.*
import cats.implicits.*
import common.api.headers.{`Pagination-Applied-Filter`, `Pagination-Total-Count`}
import common.model.ErrorIssueLocation.PathParams
import common.model.{ErrorCode, ErrorIssue}
import common.model.Kaboom.ApiKaboom
import foo.api.dto.*
import foo.api.dto.CircleCodecs.given
import foo.api.dto.FooItemApiId.toApiString
import foo.api.dto.FooItemsFilterApi.given
import foo.domain.FooItemsService
import foo.domain.model.NewFooItem
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
    for
      filter <- FooItemsFilterApi(itemName, itemText, itemType).raiseIoErrorOnFailure
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
            case None =>
              IO.raiseError(ApiKaboom(
                message = "Foo Item not found.",
                code = ErrorCode.NotFound,
                issues = NonEmptyList.one(
                  ErrorIssue(PathParams, ":fooItemId", s"couldn't find ${idVal.toApiString}"),
                ).some,
                cause = None,
              ))
        yield resp
      case Invalid(es) =>
        IO.raiseError(ApiKaboom(
          message = "Invalid path params.",
          code = ErrorCode.Invalid,
          issues = NonEmptyList.one(
            ErrorIssue(PathParams, ":fooItemId", es.foldLeft("")(_ ++ _.message)),
          ).some,
          cause = None,
        ))

}
