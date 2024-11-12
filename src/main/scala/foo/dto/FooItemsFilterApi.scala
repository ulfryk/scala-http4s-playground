package foo.dto

import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits.*
import common.helpers.swapInnerValidated
import foo.model.{FooItemName, FooItemType, FooItemsFilter}
import org.http4s.ParseFailure

object FooItemsFilterApi:
  given toRaw: (FooItemsFilter => Seq[(String, String)]) = { f =>
    List(
      f.name.map(n => ("name", n.value)),
      f.text.map(t => ("text", t)),
      f.`type`.map(tp => ("type", tp.toList.mkString("|")))
    ).flatten
  }

  def apply(
    nameParams: Option[ValidatedNel[ParseFailure, FooItemName]],
    textParams: Option[ValidatedNel[ParseFailure, Txt]],
    typeParams: ValidatedNel[ParseFailure, List[FooItemType]],
  ): ValidatedNel[ParseFailure, FooItemsFilter] =
    (nameParams.swapInnerValidated, textParams.swapInnerValidated, typeParams).mapN { (n, tx, t) =>
      FooItemsFilter(n, tx.map(_.txt), NonEmptyList.fromList(t))
    }