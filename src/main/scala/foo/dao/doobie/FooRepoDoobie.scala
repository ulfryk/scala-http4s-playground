package foo.dao.doobie

import cats.Functor
import cats.effect.Async
import doobie.*
import doobie.implicits.*
import foo.domain.FooRepo
import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

final class FooRepoDoobie[F[_] : Async : Functor] private(tx: Transactor[F]) extends FooRepo[F]:

  override def createItem(fooItem: NewFooItem): F[FooItem] =
    sql"""
      INSERT INTO foo_items (item_name, item_text, item_type)
      VALUES ($fooItem)
    """
      .update
      .withUniqueGeneratedKeys[FooItem]("id", "item_name", "item_text", "item_type")
      .transact(tx)

  override def findItem(id: FooItemId): F[Option[FooItem]] =
    sql"SELECT * FROM foo_items WHERE id = $id"
      .query[FooItem]
      .option
      .transact(tx)

  override def findItems(filter: FooItemsFilter): F[List[FooItem]] =
    (sql"SELECT * FROM foo_items" ++ FooItemsDoobieQuery(filter).asFragment)
      .query[FooItem]
      .to[List]
      .transact(tx)

object FooRepoDoobie:
  def apply[F[_] : Async](tx: Transactor[F]): FooRepo[F] = new FooRepoDoobie(tx)
