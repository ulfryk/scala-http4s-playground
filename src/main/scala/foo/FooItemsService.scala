package foo

import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.implicits.*
import foo.dao.FooRepo
import foo.dto.FooItemsFilter
import foo.model.{FooItem, FooItemId}

class FooItemsService[F[_]: Concurrent : Console](repo: FooRepo[F]):

  def getAll(filter: FooItemsFilter): F[(FooItemsFilter, List[FooItem])] =
    for
      found <- repo.findItem()
    yield (filter, found.filter(i =>
      filter.`type`.fold(true)(_.toList.contains(i.`type`)) &&
        filter.name.fold(true)(f => i.name.value.toLowerCase.contains(f.value.toLowerCase))
    ))

  def getOne(id: FooItemId): F[Option[FooItem]] =
    for
      found <- repo.findItem()
    yield found.find(_.id == id)
