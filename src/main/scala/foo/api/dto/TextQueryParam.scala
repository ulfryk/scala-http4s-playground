package foo.api.dto

import cats.data.Validated.{invalidNel, validNel}
import cats.data.ValidatedNel
import foo.domain.model.FooItemText
import org.http4s.dsl.io.OptionalValidatingQueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}

object TextQueryParam:
  object Matcher extends OptionalValidatingQueryParamDecoderMatcher[FooItemText]("text")

  private def validateFooItemTextFromString(i: String): ValidatedNel[ParseFailure, FooItemText] =
    if (i.isEmpty || i.isBlank || i.trim.length < 3) invalidNel {
      ParseFailure(sanitized = s"invalid name '$i'", details = "")
    }
    else validNel(FooItemText(i))

  private given textQueryParamDecoder: QueryParamDecoder[FooItemText] =
    QueryParamDecoder[String].emapValidatedNel(validateFooItemTextFromString)