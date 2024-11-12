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
  text.imap(FooItemName.apply) { case FooItemName(n) => n }

private[skunk] val encFooName: Encoder[FooItemName] = codecFooName.asEncoder
private[skunk] val decFooName: Decoder[FooItemName] = codecFooName.asDecoder

private val codecFooText: Codec[FooItemText] =
  text.imap(FooItemText.apply) { case FooItemText(t) => t }

private[skunk] val encFooText: Encoder[FooItemText] = codecFooText.asEncoder
private[skunk] val decFooText: Decoder[FooItemText] = codecFooText.asDecoder

private[skunk] val encNewFooItem: Encoder[NewFooItem] =
  (encFooName, encFooText, encFooType).tupled.contramap {
    case NewFooItem(fooName, fooText, fooType) => (fooName, fooText, fooType)
  }

private[skunk] val decFooItem: Decoder[FooItem] =
  (decFooId, decFooName, decFooText, decFooType).tupled.map {
    case (id, fooName, fooText, fooType) => FooItem(id, fooName, fooText, fooType)
  }
