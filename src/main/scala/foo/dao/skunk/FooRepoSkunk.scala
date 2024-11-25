package foo.dao.skunk

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import foo.domain.FooRepoTF
import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}
import skunk.*
import skunk.syntax.all.*

final class FooRepoSkunk[F[_] : Concurrent : Console] private(private val session: Session[F]) extends FooRepoTF[F]:

  def createItem(fooItem: NewFooItem): F[FooItem] =
    session.prepare(
      sql"""
        INSERT INTO foo_items (item_name, item_text, item_type)
        VALUES ($encNewFooItem)
        RETURNING *
      """.query(decFooItem)
    ).flatMap(_.unique(fooItem))

  def createMany(input: List[NewFooItem]): F[Int] = ???

  def findItem(id: FooItemId): F[Option[FooItem]] =
    for
      query <- session.prepare(sql"SELECT * FROM foo_items WHERE id = $codecFooId".query(decFooItem))
      found <- query.option(id)
    yield found

  def findItems(filter: FooItemsFilter): F[List[FooItem]] =
    val applied = listQuery(FooItemsSkunkQuery(filter))
    session.prepare(applied.fragment.query(decFooItem))
      .flatMap(_.stream(applied.argument, 16).compile.toList)

  private def listQuery(query: FooItemsSkunkQuery): AppliedFragment =
    val base = sql"SELECT * FROM foo_items"
    base(Void) |+| query.asFragment

object FooRepoSkunk:
  def apply[F[_] : Concurrent : Console](session: Session[F]): FooRepoTF[F] =
    new FooRepoSkunk(session)
