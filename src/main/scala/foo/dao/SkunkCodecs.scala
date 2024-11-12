package foo.dao

import cats.syntax.all.*
import foo.model.*
import skunk.*
import skunk.codec.all.*

private val codecFooId: Codec[FooItemId] =
  int8.imap(FooItemId.apply) { 
      case FooItemId(idVal) => idVal
  }
  
private[dao] val encFooId: Encoder[FooItemId] = codecFooId.asEncoder
private[dao] val decFooId: Decoder[FooItemId] = codecFooId.asDecoder

private val codecFooType: Codec[FooItemType] =
  varchar(256).imap(FooItemType.valueOf) {
    _.toString
  }

private[dao] val encFooType: Encoder[FooItemType] = codecFooType.asEncoder
private[dao] val decFooType: Decoder[FooItemType] = codecFooType.asDecoder

private val codecFooName: Codec[FooItemName] =
  text.imap(FooItemName.apply) {
    _.value
  }

private[dao] val encFooName: Encoder[FooItemName] = codecFooName.asEncoder
private[dao] val decFooName: Decoder[FooItemName] = codecFooName.asDecoder

private[dao] val encNewFooItem: Encoder[NewFooItem] =
  (encFooName, text, encFooType).tupled.contramap {
    case NewFooItem(fooName, fooText, fooType) => (fooName, fooText, fooType)
  }

private[dao] val decFooItem: Decoder[FooItem] =
  (decFooId, decFooName, text, decFooType).tupled.map {
    case (id, fooName, fooText, fooType) => FooItem(id, fooName, fooText, fooType)
  }
