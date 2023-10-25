package foo.dto

import cats.data.{Validated, ValidatedNel}
import foo.model.FooItemType
import org.http4s.dsl.io.OptionalMultiQueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}

import scala.util.Try


object TypeQueryParam:
  object Matcher extends OptionalMultiQueryParamDecoderMatcher[FooItemType]("type")

  def validateFooItemTypeFromString(i: String): ValidatedNel[ParseFailure, FooItemType] =
    Validated.fromTry(Try(FooItemType.valueOf(i)))
      .leftMap(t => ParseFailure(t.getMessage, t.getMessage))
      .toValidatedNel

  implicit val typeQueryParamDecoder: QueryParamDecoder[FooItemType] =
    QueryParamDecoder[String].emapValidatedNel(validateFooItemTypeFromString)

