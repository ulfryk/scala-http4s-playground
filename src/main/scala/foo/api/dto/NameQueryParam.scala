package foo.api.dto

import cats.data.Validated.{invalidNel, validNel}
import cats.data.ValidatedNel
import foo.domain.model.FooItemName
import org.http4s.dsl.io.OptionalValidatingQueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}

object NameQueryParam:
  object Matcher extends OptionalValidatingQueryParamDecoderMatcher[FooItemName]("name")

  private def validateFooItemNameFromString(i: String): ValidatedNel[ParseFailure, FooItemName] =
   if (i.isEmpty || i.isBlank || i.trim.length < 3) invalidNel {
      ParseFailure(sanitized = s"invalid name '$i'", details = "")
    }
    else validNel(FooItemName(i))

  private given nameQueryParamDecoder: QueryParamDecoder[FooItemName] =
    QueryParamDecoder[String].emapValidatedNel(validateFooItemNameFromString)
