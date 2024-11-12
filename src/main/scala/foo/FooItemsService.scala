package foo

import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.implicits.*
import foo.dao.FooRepo
import foo.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

class FooItemsService[F[_] : Concurrent : Console](repo: FooRepo[F]):

  def create(input: NewFooItem): F[FooItem] =
    repo.createItem(input)

  def getAll(filter: FooItemsFilter): F[(FooItemsFilter, List[FooItem])] =
    repo.findItems(filter).map((filter, _))

  def getOne(id: FooItemId): F[Option[FooItem]] =
    repo.findItem(id)
