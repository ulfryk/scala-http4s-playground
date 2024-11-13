package foo.api.dto

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.effect.IO
import cats.implicits.*
import common.helpers.swapInnerValidated
import foo.domain.model.{FooItemName, FooItemText, FooItemType, FooItemsFilter}
import org.http4s.ParseFailure

object FooItemsFilterApi:
  given toRaw: (FooItemsFilter => Seq[(String, String)]) = { f =>
    List(
      f.name.map { case FooItemName(n) => ("name", n) },
      f.text.map { case FooItemText(t) => ("text", t) },
      f.`type`.map(tp => ("type", tp.toList.mkString("|")))
    ).flatten
  }

  def apply(
    nameParams: Option[ValidatedNel[ParseFailure, FooItemName]],
    textParams: Option[ValidatedNel[ParseFailure, FooItemText]],
    typeParams: ValidatedNel[ParseFailure, List[FooItemType]],
  ): ValidatedNel[ParseFailure, FooItemsFilter] =
    (nameParams.swapInnerValidated, textParams.swapInnerValidated, typeParams).mapN { (n, tx, t) =>
      FooItemsFilter(n, tx, NonEmptyList.fromList(t))
    }

final case class MalformedFilter(errs: NonEmptyList[ParseFailure]) extends RuntimeException:
  override def getMessage = s"kaboom - failed to parse query filter"

extension (v: ValidatedNel[ParseFailure, FooItemsFilter])
  def raiseIoErrorOnFailure: IO[FooItemsFilter] = v match
    case Invalid(e) => IO.raiseError(MalformedFilter(e))
    case Valid(filter) => IO.pure(filter)
