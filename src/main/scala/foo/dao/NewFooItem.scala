package foo.dao

import cats.syntax.all.*
import foo.model.{FooItemName, FooItemType, NewFooItem}
import skunk.Codec
import skunk.syntax.all.*
import skunk.codec.all.*

val newFooItem: Codec[NewFooItem] =
  (text, text, varchar(256)).tupled.imap {
    case (fooName, fooText, fooType) =>
      NewFooItem(FooItemName(fooName), fooText, FooItemType.values.find(_.toString == fooType).get)
  } {
    case NewFooItem(FooItemName(fooName), fooText, fooType) => (fooName, fooText, fooType.toString)
  }
