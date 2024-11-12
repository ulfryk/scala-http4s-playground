package foo.dao

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import foo.dto.FooItemsFilter
import foo.model.*
import skunk.*
import skunk.syntax.all.*

final class FooRepoSkunk[F[_] : Concurrent : Console] private(private val session: Session[F]) extends FooRepo[F]:

  override def createItem(fooItem: NewFooItem): F[FooItem] =
    session.prepare(
      sql"""
        INSERT INTO foo_items (item_name, item_text, item_type)
        VALUES ($encNewFooItem)
        RETURNING *
      """.query(decFooItem)
    ).flatMap(_.unique(fooItem))

  override def findItem(id: FooItemId): F[Option[FooItem]] =
    for
      query <- session.prepare(sql"SELECT * FROM foo_items WHERE id = $encFooId".query(decFooItem))
      found <- query.option(id)
    yield found

  override def findItems(filter: FooItemsFilter): F[List[FooItem]] =
    val applied = listQuery(FooItemsSkunkQuery(filter))
    session.prepare(applied.fragment.query(decFooItem))
      .flatMap(_.stream(applied.argument, 16).compile.toList)

  private def listQuery(query: FooItemsSkunkQuery): AppliedFragment =
    val base = sql"SELECT * FROM foo_items"
    base(Void) |+| query.asFragment

object FooRepoSkunk:
  def apply[F[_] : Concurrent : Console](session: Session[F]): F[FooRepo[F]] =
    new FooRepoSkunk(session).pure[F]