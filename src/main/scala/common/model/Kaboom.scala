package common.model

import cats.data.NonEmptyList

enum Kaboom extends RuntimeException {
  case ApiKaboom(
    message: String,
    code: ErrorCode,
    issues: Option[NonEmptyList[ErrorIssue]],
    cause: Option[Throwable],
  )
  
  case InternalKaboom(
    message: String,
    cause: Option[Throwable],
  )
}

enum ErrorCode {
  case Invalid
  case Conflict
  case Internal
  case NotFound
}

final case class ErrorIssue(
  location: ErrorIssueLocation,
  position: String,
  description: String,
)

enum ErrorIssueLocation {
  case QueryParams
  case PathParams
  case Payload
  case Internal
}
