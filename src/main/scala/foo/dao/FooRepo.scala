package foo.dao

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import foo.model.{FooItem, FooItemId, FooItemName, FooItemType, NewFooItem}
import skunk.*
import skunk.syntax.all.*
import skunk.Codec
import skunk.codec.all.*

class FooRepo[F[_] : Concurrent : Console] private (private val session: Session[F]) {

  def createItem(fooItem: NewFooItem): F[Unit] =
    for {
      command <- session.prepare(sql"INSERT INTO foo_items (item_name, item_text, item_type) values ($newFooItem)".command)
      rowCount <- command.execute(fooItem)
      _ <- Console[F].println(s"Inserted $rowCount rows")
    } yield ()

  def findItem(): F[List[FooItem]] =
    for {
      query <- session.prepare(sql"SELECT * FROM foo_items".query(fooItem))
      found <- query.stream(skunk.Void, 16).compile.toList
    } yield found

  def findItem(name: FooItemName): F[Unit] =
    for {
      query <- session.prepare(sql"SELECT * FROM foo_items WHERE item_name = $varchar".query(fooItem))
      found <- query.stream(name.value, 16).compile.toList
      _ <- Console[F].println(s"found users: $found")
    } yield ()

  private val newFooItem: Codec[NewFooItem] =
    (text, text, varchar(256)).tupled.imap {
      case (fooName, fooText, fooType) =>
        NewFooItem(FooItemName(fooName), fooText, FooItemType.valueOf(fooType))
    } {
      case NewFooItem(FooItemName(fooName), fooText, fooType) => (fooName, fooText, fooType.toString)
    }
    
  private val fooItem: Codec[FooItem] =
    (int8, text, text, varchar(256)).tupled.imap {
      case (id, fooName, fooText, fooType) =>
        FooItem(FooItemId(id), FooItemName(fooName), fooText, FooItemType.valueOf(fooType))
    } {
      case FooItem(FooItemId(id), FooItemName(fooName), fooText, fooType) => (id, fooName, fooText, fooType.toString)
    }

}

object FooRepo:
  def apply[F[_] : Concurrent : Console](session: Session[F]): F[FooRepo[F]] =
    new FooRepo(session).pure[F]
