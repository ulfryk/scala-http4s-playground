package foo.dao

import foo.dto.FooItemsFilter
import foo.model.{FooItem, FooItemId, NewFooItem}

trait FooRepo[F[_]]:

  def createItem(fooItem: NewFooItem): F[FooItem]

  def findItem(id: FooItemId): F[Option[FooItem]]

  def findItems(filter: FooItemsFilter): F[List[FooItem]]
