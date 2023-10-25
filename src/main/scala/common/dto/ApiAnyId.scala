package common.dto

import cats.data.Validated.invalidNec
import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import common.model.{AnyId, IdParsingFail}


sealed trait InvalidPathId:
  val message: String

case class MissingId() extends InvalidPathId:
  val message = s"foo item id not provided"

case class MalformedId(found: String, cause: IdParsingFail) extends InvalidPathId:
  val message = s"kaboom - $found is not a valid FooItemId (${cause.getClass.getSimpleName})"

trait ApiAnyId[T <: AnyId]:
  protected val impl: T

  def unapply(str: String): Option[ValidatedNec[InvalidPathId, impl.Id]] =
    if (str.isEmpty) invalidNec(MissingId()).some
    else impl.fromString(str).leftMap(_.map(e => MalformedId(str, e))).some
