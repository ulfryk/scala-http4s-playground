package foo.dto

import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits.*
import common.helpers.swapInnerValidated
import foo.model.{FooItemName, FooItemType}
import org.http4s.ParseFailure


case class FooItemsFilter(
  name: Option[FooItemName],
  `type`: Option[NonEmptyList[FooItemType]],
)

object FooItemsFilter:
  final val empty = FooItemsFilter(None, None)

  def apply(
    nameParams: Option[ValidatedNel[ParseFailure, FooItemName]],
    typeParams: ValidatedNel[ParseFailure, List[FooItemType]],
  ): ValidatedNel[ParseFailure, FooItemsFilter] =
    (nameParams.swapInnerValidated, typeParams).mapN { (n, t) =>
      FooItemsFilter(n, NonEmptyList.fromList(t))
    }
