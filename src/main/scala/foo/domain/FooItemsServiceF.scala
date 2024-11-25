package foo.domain

import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.free.Free
import cats.~>
import foo.domain.FooQueryF.{Create, Get, GetList}
import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

class FooItemsServiceF[F[_] : Concurrent : Console](natTrans: FooQueryF ~> F) extends FooItemsService[F]:
  private def createF(inp: NewFooItem): Free[FooQueryF, FooItem] = Free.liftF(Create(inp))
  private def readF(id: FooItemId): Free[FooQueryF, Option[FooItem]] = Free.liftF(Get(id))
  private def readAllF(f: FooItemsFilter): Free[FooQueryF, List[FooItem]] = Free.liftF(GetList(f))

  def create(input: NewFooItem): F[FooItem] = createF(input).foldMap(natTrans)
  def getAll(filter: FooItemsFilter): F[List[FooItem]] = readAllF(filter).foldMap(natTrans)
  def getOne(id: FooItemId): F[Option[FooItem]] = readF(id).foldMap(natTrans)
  def createMany(input: List[NewFooItem]): F[Int] = ???
