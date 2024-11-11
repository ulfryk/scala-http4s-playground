package foo.dao

import cats.*
import cats.data.NonEmptyList
import cats.syntax.all.*
import foo.dto.FooItemsFilter
import foo.model.{FooItemName, FooItemType}
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

case class FooItemsQuery(
  itemName: Option[AppliedFragment],
  itemType: Option[AppliedFragment],
) {
  def asFragment: AppliedFragment =
    val conditions = List(itemName, itemType).flatten
    if (conditions.isEmpty) AppliedFragment.empty
    else conditions.foldSmash(void" WHERE ", void" AND ", AppliedFragment.empty)
}

object FooItemsQuery:
  def apply(filter: FooItemsFilter): FooItemsQuery =
    new FooItemsQuery(
      itemName = filter.name.map(nameSql),
      itemType = filter.`type`.map(typeMultiSql)
    )

  private val encFooNameILike: Encoder[FooItemName] = text.contramap(v => s"%${v.value}%")
  private val nameSql = sql"item_name ILIKE $encFooNameILike"

  private def typeMultiSql(types: NonEmptyList[FooItemType]) =
    if (types.tail.isEmpty) sql"item_type = $encFooType"(types.head)
    else sql"item_type IN (${encFooType.list(types.size)})"(types.toList)
