package foo.dao

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import foo.dto.FooItemsFilter
import foo.model.{FooItem, FooItemId, FooItemName, FooItemType, NewFooItem}
import skunk.*
import skunk.syntax.all.*
import skunk.Codec
import skunk.codec.all.*

class FooRepo[F[_] : Concurrent : Console] private (private val session: Session[F]) {

  def createItem(fooItem: NewFooItem): F[Unit] =
    for 
      command <- session.prepare(sql"INSERT INTO foo_items (item_name, item_text, item_type) values ($encNewFooItem)".command)
      rowCount <- command.execute(fooItem)
      _ <- Console[F].println(s"Inserted $rowCount rows")
    yield ()

  def findItem(id: FooItemId): F[Option[FooItem]] =
    for
      query <- session.prepare(sql"SELECT * FROM foo_items WHERE id = $int8".query(decFooItem))
      found <- id match
        case FooItemId(idVal) => query.option(idVal)
    yield found

  def findItems(filter: FooItemsFilter): F[List[FooItem]] =
    filter match
      case FooItemsFilter(None, None) => session.prepare(sql"SELECT * FROM foo_items".query(decFooItem))
        .flatMap { query =>
          query.stream(skunk.Void, 16).compile.toList
        }
      case FooItemsFilter(Some(name), None) => session.prepare(sql"SELECT * FROM foo_items WHERE item_name ILIKE $text".query(decFooItem))
        .flatMap { query =>
          query.stream(s"%${name.value}%", 16).compile.toList
        }
      case FooItemsFilter(None, Some(fooType)) => session.prepare(sql"SELECT * FROM foo_items WHERE item_type = ${varchar(256)}".query(decFooItem))
        .flatMap { query =>
          query.stream(fooType.head.toString, 16).compile.toList
        }
      case FooItemsFilter(Some(name), Some(fooType)) => session.prepare(sql"SELECT * FROM foo_items WHERE item_name ILIKE $text AND item_type = ${varchar(256)}".query(decFooItem))
        .flatMap { query =>
          query.stream((s"%${name.value}%", fooType.head.toString), 16).compile.toList
        }

  private val encNewFooItem: Encoder[NewFooItem] =
    (text, text, varchar(256)).tupled.contramap {
      case NewFooItem(FooItemName(fooName), fooText, fooType) => (fooName, fooText, fooType.toString)
    }
    
  private val decFooItem: Decoder[FooItem] =
    (int8, text, text, varchar(256)).tupled.map {
      case (id, fooName, fooText, fooType) =>
        FooItem(FooItemId(id), FooItemName(fooName), fooText, FooItemType.valueOf(fooType))
    }
}

object FooRepo:
  def apply[F[_] : Concurrent : Console](session: Session[F]): F[FooRepo[F]] =
    new FooRepo(session).pure[F]
