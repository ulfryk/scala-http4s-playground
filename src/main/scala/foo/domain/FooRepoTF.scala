package foo.domain

import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

trait FooRepoTF[F[_]]:
  def createItem(fooItem: NewFooItem): F[FooItem]
  def findItem(id: FooItemId): F[Option[FooItem]]
  def findItems(filter: FooItemsFilter): F[List[FooItem]]
