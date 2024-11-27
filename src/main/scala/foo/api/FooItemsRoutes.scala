package foo.api

import _root_.io.circe.*
import _root_.io.circe.generic.auto.*
import _root_.io.circe.syntax.*
import cats.MonadThrow
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import common.api.headers.{`Pagination-Applied-Filter`, `Pagination-Total-Count`}
import common.helpers.*
import foo.api.dto.*
import foo.api.dto.CircleCodecs.given
import foo.api.dto.FooItemApiId.toApiString
import foo.api.dto.FooItemsCsv.csvRowDecoder
import foo.api.dto.FooItemsFilterApi.given
import foo.api.error.*
import foo.domain.FooItemsService
import foo.domain.model.{FooItemType, NewFooItem}
import fs2.data.csv.*
import fs2.text
import org.http4s.*
import org.http4s.Header.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.typelevel.ci.*

def fooItemsRoutes(service: FooItemsService[IO])(using MonadThrow[IO]) = HttpRoutes.of[IO] {

  // Propose fix - add "name" prop to `Content-Type`
  case req @ POST -> Root / "foo-items" => req.headers.get(ci"Content-Type") match {
    case Some(NonEmptyList(contentType, Nil)) => contentType.value match
      case "application/json" =>
        for
          input <- req.as[NewFooItem]
          created <- service.create(input)
          resp <- Ok(created.asJson)
        yield resp

      case "text/csv" =>
        for
          input <- req.body
            .through(text.utf8.decode)
            .through(decodeUsingHeaders[NewFooItem]())
            .compile
            .toList
          created <- service.createMany(input)
          resp <- Ok(created.asJson)
        yield resp.putHeaders("X-Created-Count" -> created.toString)

      case theContentType => IO.raiseError(
        invalidHeaders(NonEmptyList.of(("Content-Type", s"'$theContentType' not supported."))))

    case _ => IO.raiseError(
      invalidHeaders(NonEmptyList.of(("Content-Type", s"Missing."))))
  }

  case GET -> Root / "foo-items"
    :? TypeQueryParam.Matcher(itemType)
    +& NameQueryParam.Matcher(itemName)
    +& TextQueryParam.Matcher(itemText) =>
    val parsedFilter = FooItemsFilterApi(itemName, itemText, itemType)
    for
      filter <- parsedFilter.raiseErrorOnInvalid(invalidQParams)
      data <- service.getAll(filter)
      resp <- Ok(data.asJson)
    yield resp.putHeaders(`Pagination-Total-Count`(data.size), `Pagination-Applied-Filter`(filter))

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    for
      idVal <- id.raiseErrorOnInvalid { es =>
        invalidPParams(":fooItemId", es.foldLeft("")(_ ++ _.message))
      }
      maybeFound <- service.getOne(idVal)
      found <- maybeFound.raiseErrorOnNone(notFound(":fooItemId", idVal.toApiString))
      resp <- Ok(found.asJson)
    yield resp

}
