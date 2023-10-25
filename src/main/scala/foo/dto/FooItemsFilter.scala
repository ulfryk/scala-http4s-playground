package foo.dto

import cats.data.Validated.validNel
import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits.*
import common.helpers.swapInnerValidated
import foo.model.{FooItemName, FooItemType}
import org.http4s.ParseFailure


case class FooItemsFilter(name: Option[FooItemName], `type`: Option[NonEmptyList[FooItemType]])

object FooItemsFilter:
  
  def apply(
    nameParams: Option[ValidatedNel[ParseFailure, FooItemName]],
    typeParams: ValidatedNel[ParseFailure, List[FooItemType]],
  ): ValidatedNel[ParseFailure, FooItemsFilter] =
    (nameParams.swapInnerValidated, typeParams).mapN { (n, t) =>
      FooItemsFilter(n, NonEmptyList.fromList(t))
    }
  
  object Matcher:
    def unapply(
      params: Map[String, collection.Seq[String]]
    ): Option[ValidatedNel[ParseFailure, FooItemsFilter]] =
      for {
        nameParams <- NameQueryParam.Matcher.unapply(params)
        typeParams <- TypeQueryParam.Matcher.unapply(params)
      } yield FooItemsFilter(nameParams, typeParams)

  object Matcher2:
    def unapply(
      params: Map[String, collection.Seq[String]]
    ): Option[ValidatedNel[ParseFailure, FooItemsFilter]] =

      val nameParams = params.get("name").flatMap(_.headOption)
        .map(NameQueryParam.validateFooItemNameFromString)

      val typeParams = params.get("type")
        .map(_.toList.traverse(TypeQueryParam.validateFooItemTypeFromString))
        .getOrElse(validNel(Nil))

      FooItemsFilter(nameParams, typeParams).some
