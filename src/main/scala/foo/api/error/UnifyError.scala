package foo.api.error

import cats.implicits.*
import common.model.Kaboom.{ApiKaboom, InternalKaboom}
import common.model.{ErrorCode, ErrorIssue, ErrorIssueLocation, Kaboom}
import foo.api.dto.MalformedFilter


def unifyError(err: Throwable): Kaboom = err match
  case err: Kaboom => err
  case err @ MalformedFilter(errs) => ApiKaboom(
    message = err.getMessage,
    code = ErrorCode.Invalid,
    issues = errs.map(w => ErrorIssue(ErrorIssueLocation.QueryParams, "query", w.sanitized)).some,
    cause = err.some,
  )
  case x => InternalKaboom(
    message = "Something went wrong.",
    cause = x.some,
  )