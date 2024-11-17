package foo.domain

import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

trait FooItemsService[F[_]]:
  def create(input: NewFooItem): F[FooItem]
  def getAll(filter: FooItemsFilter): F[List[FooItem]]
  def getOne(id: FooItemId): F[Option[FooItem]]
