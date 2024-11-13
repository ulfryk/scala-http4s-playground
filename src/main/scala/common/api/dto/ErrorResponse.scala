package common.api.dto

import cats.data.NonEmptyList
import common.model.Kaboom.{ApiKaboom, InternalKaboom}
import common.model.{ErrorCode, ErrorIssue, ErrorIssueLocation, Kaboom}
import io.circe.Encoder

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

  given errorCodeEncoder: Encoder[ErrorCode] =
    Encoder[String].contramap(_.toString)
  
  given errorIssueLocationEncoder: Encoder[ErrorIssueLocation] =
    Encoder[String].contramap(_.toString)
