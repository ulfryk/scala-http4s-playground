package foo.dao.skunk

import cats.syntax.all.*
import foo.domain.model.{FooItem, FooItemId, FooItemName, FooItemText, FooItemType, NewFooItem}
import skunk.*
import skunk.codec.all.*

private[skunk] val codecFooId: Codec[FooItemId] =
  int8.imap(FooItemId.apply) { case FooItemId(id) => id }

private[skunk] val codecFooType: Codec[FooItemType] =
  varchar(256).imap(FooItemType.valueOf)(_.toString)

private[skunk] val codecFooName: Codec[FooItemName] =
  text.imap(FooItemName.apply) { case FooItemName(n) => n }

private[skunk] val codecFooText: Codec[FooItemText] =
  text.imap(FooItemText.apply) { case FooItemText(t) => t }

private[skunk] val encNewFooItem: Encoder[NewFooItem] =
  (codecFooName, codecFooText, codecFooType).tupled.contramap {
    case NewFooItem(fooName, fooText, fooType) => (fooName, fooText, fooType)
  }

private[skunk] val decFooItem: Decoder[FooItem] =
  (codecFooId, codecFooName, codecFooText, codecFooType).tupled.map {
    case (id, fooName, fooText, fooType) => FooItem(id, fooName, fooText, fooType)
  }
