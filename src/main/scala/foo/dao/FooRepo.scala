package foo.dao

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import foo.dto.FooItemsFilter
import foo.model.*
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

class FooRepo[F[_] : Concurrent : Console] private(private val session: Session[F]) {

  def createItem(fooItem: NewFooItem): F[FooItem] =
    session.prepare(
      sql"""
        INSERT INTO foo_items (item_name, item_text, item_type)
        VALUES ($encNewFooItem)
        RETURNING *
      """.query(decFooItem)
    ).flatMap(_.unique(fooItem))

  def findItem(id: FooItemId): F[Option[FooItem]] =
    for
      query <- session.prepare(sql"SELECT * FROM foo_items WHERE id = $int8".query(decFooItem))
      found <- id match
        case FooItemId(idVal) => query.option(idVal)
    yield found

  def findItems(filter: FooItemsFilter): F[List[FooItem]] =
    val applied = listQuery(FooItemsQuery(filter))
    session.prepare(applied.fragment.query(decFooItem))
      .flatMap(_.stream(applied.argument, 16).compile.toList)

  private def listQuery(query: FooItemsQuery): AppliedFragment =
    val base = sql"SELECT * FROM foo_items"
    base(Void) |+| query.asFragment

}

object FooRepo:
  def apply[F[_] : Concurrent : Console](session: Session[F]): F[FooRepo[F]] =
    new FooRepo(session).pure[F]
