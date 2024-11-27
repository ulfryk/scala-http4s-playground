package foo.domain

import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

class FooItemsServiceTF[F[_] : Concurrent : Console](repo: FooRepoTF[F]) extends FooItemsService[F]:
  def create(input: NewFooItem): F[FooItem] = repo.createItem(input)
  def createMany(input: List[NewFooItem]): F[List[FooItem]] = repo.createMany(input)
  def getAll(filter: FooItemsFilter): F[List[FooItem]] = repo.findItems(filter)
  def getOne(id: FooItemId): F[Option[FooItem]] = repo.findItem(id)
