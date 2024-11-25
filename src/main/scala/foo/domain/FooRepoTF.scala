package foo.domain

import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

trait FooRepoTF[F[_]]:
  def createItem(fooItem: NewFooItem): F[FooItem]
  def createMany(input: List[NewFooItem]): F[Int]
  def findItem(id: FooItemId): F[Option[FooItem]]
  def findItems(filter: FooItemsFilter): F[List[FooItem]]
