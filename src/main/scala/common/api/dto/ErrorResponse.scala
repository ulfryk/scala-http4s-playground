package common.api.dto

import _root_.io.circe.*
import _root_.io.circe.generic.auto.*
import _root_.io.circe.syntax.*
import cats.data.NonEmptyList
import cats.effect.IO
import common.model.Kaboom.{ApiKaboom, InternalKaboom}
import common.model.{ErrorCode, ErrorIssue, ErrorIssueLocation, Kaboom}
import org.http4s.Response
import org.http4s.circe.*
import org.http4s.dsl.io.*

final case class ErrorResponse(
  message: String,
  code: ErrorCode,
  issues: Option[NonEmptyList[ErrorIssue]],
)

object ErrorResponse:
  def apply(err: Kaboom): ErrorResponse =
    err match
      case ApiKaboom(message, code, issues, _) => new ErrorResponse(message, code, issues)
      case InternalKaboom(message, _) => new ErrorResponse(message, ErrorCode.Internal, None)

  def toResponse(err: Kaboom): IO[Response[IO]] =
    err match
      case e: ApiKaboom =>
        val resp = ErrorResponse(e).asJson
        e.code match
          case ErrorCode.Invalid => BadRequest(resp)
          case ErrorCode.Conflict => Conflict(resp)
          case ErrorCode.Internal => InternalServerError(resp)
          case ErrorCode.NotFound => NotFound(resp)
      case e: InternalKaboom => InternalServerError(ErrorResponse(e).asJson)

  given errorCodeEncoder: Encoder[ErrorCode] =
    Encoder[String].contramap(_.toString)

  given errorIssueLocationEncoder: Encoder[ErrorIssueLocation] =
    Encoder[String].contramap(_.toString)
