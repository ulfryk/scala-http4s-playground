package foo.dao.skunk

import cats.*
import cats.data.NonEmptyList
import cats.syntax.all.*
import foo.model.{FooItemName, FooItemText, FooItemType, FooItemsFilter}
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

case class FooItemsSkunkQuery(
  itemName: Option[AppliedFragment],
  itemText: Option[AppliedFragment],
  itemType: Option[AppliedFragment],
):
  def asFragment: AppliedFragment =
    val conditions = List(itemName, itemText, itemType).flatten
    if (conditions.isEmpty) AppliedFragment.empty
    else conditions.foldSmash(void" WHERE ", void" AND ", AppliedFragment.empty)

object FooItemsSkunkQuery:
  def apply(filter: FooItemsFilter): FooItemsSkunkQuery =
    new FooItemsSkunkQuery(
      itemName = filter.name.map(nameSql),
      itemText = filter.text.map(textSql),
      itemType = filter.`type`.map(typeMultiSql)
    )

  private val encFooNameILike: Encoder[FooItemName] = text.contramap(v => s"%${v.value}%")
  private val nameSql = sql"item_name ILIKE $encFooNameILike"

  private val encFooTextILike: Encoder[FooItemText] = text.contramap(v => s"%$v%")
  private val textSql = sql"item_text ILIKE $encFooTextILike"

  private def typeMultiSql(types: NonEmptyList[FooItemType]) =
    if (types.tail.isEmpty) sql"item_type = $encFooType"(types.head)
    else sql"item_type IN (${encFooType.list(types.size)})"(types.toList)
