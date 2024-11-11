package foo.dto

import cats.data.Validated.{invalidNel, validNel}
import cats.data.ValidatedNel
import org.http4s.dsl.io.OptionalValidatingQueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}

case class Txt(txt: String)

object TextQueryParam:
  object Matcher extends OptionalValidatingQueryParamDecoderMatcher[Txt]("text")

  private def validateFooItemTextFromString(i: String): ValidatedNel[ParseFailure, Txt] =
    if (i.isEmpty || i.isBlank || i.trim.length < 3) invalidNel {
      ParseFailure(sanitized = s"invalid name '$i'", details = "")
    }
    else validNel(Txt(i))

  private given textQueryParamDecoder: QueryParamDecoder[Txt] =
    QueryParamDecoder[String].emapValidatedNel(validateFooItemTextFromString)