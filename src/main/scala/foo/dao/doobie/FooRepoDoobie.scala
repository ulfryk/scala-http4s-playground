package foo.dao.doobie

import cats.Functor
import cats.effect.Async
import doobie.*
import doobie.implicits.*
import foo.domain.FooRepoTF
import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

final class FooRepoDoobie[F[_] : Async : Functor] private(tx: Transactor[F]) extends FooRepoTF[F]:

  def createItem(fooItem: NewFooItem): F[FooItem] =
    insert.toUpdate0(fooItem)
      .withUniqueGeneratedKeys[FooItem]("id", "item_name", "item_text", "item_type")
      .transact(tx)

  def createMany(input: List[NewFooItem]): F[List[FooItem]] =
    insert
      .updateManyWithGeneratedKeys[FooItem]("id", "item_name", "item_text", "item_type")(input)
      .transact(tx)
      .compile.toList

  private def insert = Update[NewFooItem](
    "INSERT INTO foo_items (item_name, item_text, item_type) VALUES (?, ?, ?)")

  def findItem(id: FooItemId): F[Option[FooItem]] =
    sql"SELECT * FROM foo_items WHERE id = $id"
      .query[FooItem]
      .option
      .transact(tx)

  def findItems(filter: FooItemsFilter): F[List[FooItem]] =
    (sql"SELECT * FROM foo_items" ++ FooItemsDoobieQuery(filter).asFragment)
      .query[FooItem]
      .to[List]
      .transact(tx)

object FooRepoDoobie:
  def apply[F[_] : Async](tx: Transactor[F]): FooRepoTF[F] = new FooRepoDoobie(tx)
