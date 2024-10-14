package common.model

import cats.data.ValidatedNec
import common.helpers.toValidatedNec

import java.lang.Long.parseLong
import scala.util.{Failure, Success, Try}


sealed trait IdParsingFail

object IdIsEmpty extends IdParsingFail

case class IdHasSingleSegment(segment: String) extends IdParsingFail

case class IdHasTooManySegments(segments: String*) extends IdParsingFail

case class EntityDoesntMatch(prefix: String) extends IdParsingFail

case class HashMalformed(hashed: String, cause: Throwable) extends IdParsingFail


class AnyId(private val prefix: String):

  opaque type Identifier = Long

  def create(id: Long): Identifier = id

  def apply(id: Long): Identifier = id

  def fromString(id: String): ValidatedNec[IdParsingFail, Identifier] =
    (for {
      x <- splitRawId(id)
      _ <- checkPrefix(x._1)
      theId <- parseHashedValue(x._2)
    } yield theId).toValidatedNec

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
      case all@one :: two :: _ => Left(IdHasTooManySegments(all *))
      case single :: Nil => Left(IdHasSingleSegment(single))
      case Nil => Left(IdIsEmpty)

  extension (theId: Identifier)
    // how to override build in toString ?
    def toApiString: String = s"${prefix}_${theId.toHexString}"
