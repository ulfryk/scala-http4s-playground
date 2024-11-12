package foo.dao.doobie

import cats.syntax.all.*
import doobie.implicits.*
import doobie.util.Put
import doobie.{Fragment, Fragments}
import foo.dao.doobie.FooItemsDoobieQuery
import foo.model.{FooItemName, FooItemsFilter}

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
  private case class Txt(txt: String) // to avoid circular implicit dependency on string
  private given putFooNameForILike: Put[FooItemName] = Put[String].contramap(n => s"%${n.value}%")
  private given putFooTextForILike: Put[Txt] = Put[String].contramap(t => s"%${t.txt}%")
  
  def apply(filter: FooItemsFilter): FooItemsDoobieQuery =
    new FooItemsDoobieQuery(
      itemName = filter.name.map { n => fr"item_name ILIKE $n" },
      itemText = filter.text.map(Txt.apply).map { t => fr"item_text ILIKE $t"},
      itemType = filter.`type`.map { tp => Fragments.in(fr"item_type", tp)},
    )
