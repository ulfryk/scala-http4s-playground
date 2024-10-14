package foo

import foo.dto.FooItemsFilter
import foo.model.{FooItem, FooItemId, FooItemName, FooItemType}

import scala.collection.immutable.ListMap

class FooItemsService:
  private val allIds = List(54321, 54322, 6543212, 123413, 1115)
  private val ids = allIds.map(FooItemId(_).toApiString)

  private val repository: ListMap[FooItemId, FooItem] =
    val items = (FooItem(FooItemId(54321), FooItemName("first"), "asfasdfasdfas", FooItemType.PLAIN) ::
      FooItem(FooItemId(54322), FooItemName("second"), "asfasdf<br/>asdfas", FooItemType.RICH) ::
      FooItem(FooItemId(6543212), FooItemName("third"), "asfasdfasdfas", FooItemType.PLAIN) ::
      FooItem(FooItemId(123413), FooItemName("fourth"), "<p>asfasdfasdfas</p>", FooItemType.RICH) ::
      FooItem(FooItemId(1115), FooItemName("last"), "asfasdfasdfas", FooItemType.PLAIN) ::
      Nil)
      .map(item => (item.id, item))
    ListMap.from(items)

  def getAll(filter: FooItemsFilter): (FooItemsFilter, List[FooItem]) =
    (filter, repository.values.filter(i =>
      filter.`type`.fold(true)(_.toList.contains(i.`type`)) &&
        filter.name.fold(true)(f => i.name.value.toLowerCase.contains(f.value.toLowerCase))
    ).toList)

  def getOne(id: FooItemId): Option[FooItem] =
    repository.get(id)