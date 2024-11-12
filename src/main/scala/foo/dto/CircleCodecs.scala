package foo.dto

import _root_.io.circe.*
import _root_.io.circe.generic.auto.*
import cats.effect.IO
import common.dto.MalformedId
import foo.dto.FooItemApiId.toApiString
import foo.model.*
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

object CircleCodecs:
  given fooItemIdEncoder: Encoder[FooItemId] =
    Encoder[String].contramap {
      _.toApiString
    }

  given fooItemNameEncoder: Encoder[FooItemName] =
    Encoder[String].contramap { case FooItemName(n) => n }

  given fooItemTextEncoder: Encoder[FooItemText] =
    Encoder[String].contramap { case FooItemText(t) => t }

  given fooItemTypeEncoder: Encoder[FooItemType] =
    Encoder[String].contramap(_.toString)

  given fooItemIdDecoder: Decoder[FooItemId] =
    Decoder[String].emap { str =>
      FooItemApiId.parse(str)
        .toEither.left.map(
          _.toChain.map(MalformedId(str, _).message)
            .toList.mkString(", "))
    }

  given fooItemNameDecoder: Decoder[FooItemName] =
    Decoder[String].map(FooItemName.apply)

  given fooItemTextDecoder: Decoder[FooItemText] =
    Decoder[String].map(FooItemText.apply)

  given fooItemTypeDecoder: Decoder[FooItemType] =
    Decoder[String].map(FooItemType.valueOf)

  given entityDecoderNewFooItem: EntityDecoder[IO, NewFooItem] = jsonOf[IO, NewFooItem]
