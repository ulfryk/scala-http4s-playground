package foo.model

import cats.data.NonEmptyList

case class FooItemsFilter(
  name: Option[FooItemName],
  text: Option[FooItemText],
  `type`: Option[NonEmptyList[FooItemType]],
)

object FooItemsFilter:
  final val empty = FooItemsFilter(None, None, None)


