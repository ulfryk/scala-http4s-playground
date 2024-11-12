package foo.dao

import foo.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

trait FooRepo[F[_]]:

  def createItem(fooItem: NewFooItem): F[FooItem]

  def findItem(id: FooItemId): F[Option[FooItem]]

  def findItems(filter: FooItemsFilter): F[List[FooItem]]
