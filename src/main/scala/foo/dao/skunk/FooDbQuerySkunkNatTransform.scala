package foo.dao.skunk

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import foo.domain.FooQueryF
import foo.domain.FooQueryF.*
import skunk.syntax.all.*
import skunk.{Session, *}

class FooDbQuerySkunkNatTransform[F[_] : Concurrent : Console](session: Session[F]) extends (FooQueryF ~> F):

  override def apply[A](fa: FooQueryF[A]): F[A] = fa match
    case Create(inp) =>
      session.prepare(
        sql"""
          INSERT INTO foo_items (item_name, item_text, item_type)
          VALUES ($encNewFooItem)
          RETURNING *
        """.query(decFooItem)
      ).flatMap(_.unique(inp))

    case Get(id) =>
      for
        query <- session.prepare(sql"SELECT * FROM foo_items WHERE id = $codecFooId".query(decFooItem))
        found <- query.option(id)
      yield found

    case GetList(filter) =>
      val applied = listQuery(FooItemsSkunkQuery(filter))
      session.prepare(applied.fragment.query(decFooItem))
        .flatMap(_.stream(applied.argument, 16).compile.toList)

  private def listQuery(query: FooItemsSkunkQuery): AppliedFragment =
    val base = sql"SELECT * FROM foo_items"
    base(Void) |+| query.asFragment

object skunkQ:
  def apply[F[_] : Concurrent : Console](session: Session[F]): FooDbQuerySkunkNatTransform[F] =
    new FooDbQuerySkunkNatTransform(session)