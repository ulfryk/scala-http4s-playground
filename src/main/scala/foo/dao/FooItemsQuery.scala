package foo.dao

import cats.*
import cats.syntax.all.*
import foo.dto.FooItemsFilter
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
      itemName = filter.name.map(n => s"%${n.value}%").map(nameSql),
      itemType = filter.`type`.map(_.head.toString).map(typeSql)
    )

  private val nameSql = sql"item_name ILIKE $varchar"
  private val typeSql = sql" item_type = ${varchar(256)}"
