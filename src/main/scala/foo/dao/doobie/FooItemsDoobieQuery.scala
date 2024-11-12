package foo.dao.doobie

import cats.syntax.all.*
import doobie.implicits.*
import doobie.util.Put
import doobie.{Fragment, Fragments}
import foo.model.{FooItemName, FooItemText, FooItemsFilter}

final case class FooItemsDoobieQuery(
  itemName: Option[Fragment],
  itemText: Option[Fragment],
  itemType: Option[Fragment],
):

  def asFragment: Fragment =
    val conditions = List(itemName, itemText, itemType).flatten
    if (conditions.isEmpty) Fragment.empty
    else conditions.foldSmash(fr" WHERE", fr"AND", Fragment.empty)

object FooItemsDoobieQuery:
  private given putFooNameForILike: Put[FooItemName] = Put[String].contramap(n => s"%$n%")
  private given putFooTextForILike: Put[FooItemText] = Put[String].contramap(t => s"%$t%")
  
  def apply(filter: FooItemsFilter): FooItemsDoobieQuery =
    new FooItemsDoobieQuery(
      itemName = filter.name.map { n => fr"item_name ILIKE $n" },
      itemText = filter.text.map { t => fr"item_text ILIKE $t"},
      itemType = filter.`type`.map { tp => Fragments.in(fr"item_type", tp)},
    )
