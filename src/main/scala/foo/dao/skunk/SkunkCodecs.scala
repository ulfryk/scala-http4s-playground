package foo.dao.skunk

import cats.syntax.all.*
import foo.model.*
import skunk.*
import skunk.codec.all.*

private val codecFooId: Codec[FooItemId] =
  int8.imap(FooItemId.apply) {
    case FooItemId(idVal) => idVal
  }

private[skunk] val encFooId: Encoder[FooItemId] = codecFooId.asEncoder
private[skunk] val decFooId: Decoder[FooItemId] = codecFooId.asDecoder

private val codecFooType: Codec[FooItemType] =
  varchar(256).imap(FooItemType.valueOf) {
    _.toString
  }

private[skunk] val encFooType: Encoder[FooItemType] = codecFooType.asEncoder
private[skunk] val decFooType: Decoder[FooItemType] = codecFooType.asDecoder

private val codecFooName: Codec[FooItemName] =
  text.imap(FooItemName.apply) {
    _.value
  }

private[skunk] val encFooName: Encoder[FooItemName] = codecFooName.asEncoder
private[skunk] val decFooName: Decoder[FooItemName] = codecFooName.asDecoder

private[skunk] val encNewFooItem: Encoder[NewFooItem] =
  (encFooName, text, encFooType).tupled.contramap {
    case NewFooItem(fooName, fooText, fooType) => (fooName, fooText, fooType)
  }

private[skunk] val decFooItem: Decoder[FooItem] =
  (decFooId, decFooName, text, decFooType).tupled.map {
    case (id, fooName, fooText, fooType) => FooItem(id, fooName, fooText, fooType)
  }
