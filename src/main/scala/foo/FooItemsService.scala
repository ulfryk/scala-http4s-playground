package foo

import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.implicits.*
import foo.dao.FooRepo
import foo.dto.FooItemsFilter
import foo.model.{FooItem, FooItemId}

class FooItemsService[F[_] : Concurrent : Console](repo: FooRepo[F]):

  def getAll(filter: FooItemsFilter): F[(FooItemsFilter, List[FooItem])] =
    for
      found <- repo.findItems(filter)
    yield (filter, found)

  def getOne(id: FooItemId): F[Option[FooItem]] =
    repo.findItem(id)
