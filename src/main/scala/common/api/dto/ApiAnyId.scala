package common.api.dto

import cats.data.*
import cats.data.Validated.*
import cats.implicits.*
import common.helpers.some
import common.model.AnyId

import java.lang.Long.parseLong
import scala.util.{Failure, Success, Try}


sealed trait IdParsingFail

case object IdIsEmpty extends IdParsingFail

final case class IdHasSingleSegment(segment: String) extends IdParsingFail

final case class IdHasTooManySegments(segments: String*) extends IdParsingFail

final case class EntityDoesntMatch(prefix: String) extends IdParsingFail

final case class HashMalformed(hashed: String, cause: Throwable) extends IdParsingFail


sealed trait InvalidPathId:
  val message: String

final case class MissingId() extends InvalidPathId:
  val message = s"foo item id not provided"

final case class MalformedId(found: String, cause: IdParsingFail) extends InvalidPathId:
  val message = s"kaboom - $found is not a valid FooItemId (${cause.getClass.getSimpleName})"


trait ApiAnyId[T](prefix: String, impl: AnyId[T]):
  def unapply(str: String): Option[ValidatedNec[InvalidPathId, T]] =
    if (str.isEmpty) invalidNec(MissingId()).some
    else parse(str).leftMap(_.map(e => MalformedId(str, e))).some

  def parse(id: String): ValidatedNec[IdParsingFail, T] =
    (for {
      x <- splitRawId(id)
      _ <- checkPrefix(x._1)
      theId <- parseHashedValue(x._2)
    } yield impl.apply(theId)).toValidatedNec

  private def parseHashedValue(hashed: String): Either[IdParsingFail, Long] =
    Try(parseLong(hashed, 16)) match
      case Success(id) => Right(id)
      case Failure(e) => Left(HashMalformed(hashed, e))

  private def checkPrefix(foundPrefix: String): Either[IdParsingFail, String] =
    if foundPrefix == prefix then Right(foundPrefix)
    else Left(EntityDoesntMatch(foundPrefix))

  private def splitRawId(raw: String): Either[IdParsingFail, (String, String)] =
    raw.split("_").toList match
      case prefix :: value :: Nil => Right((prefix, value))
      case all@_ :: _ :: _ => Left(IdHasTooManySegments(all *))
      case single :: Nil => Left(IdHasSingleSegment(single))
      case Nil => Left(IdIsEmpty)

  extension (theId: T)
    // how to override build in toString ?
    def toApiString: String = s"${prefix}_${impl.un(theId).toHexString.reverse.padTo(10, '0').reverse}"
