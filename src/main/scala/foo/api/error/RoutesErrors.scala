package foo.api.error

import cats.data.NonEmptyList
import common.helpers.some
import common.model.ErrorIssueLocation.{PathParams, QueryParams}
import common.model.Kaboom.ApiKaboom
import common.model.{ErrorCode, ErrorIssue, ErrorIssueLocation}
import org.http4s.ParseFailure

def invalidPParams(param: String, message: String) = ApiKaboom(
  message = "Invalid path params.",
  code = ErrorCode.Invalid,
  issues = NonEmptyList.one(ErrorIssue(PathParams, param, message)).some,
  cause = None,
)

def notFound(idPathParam: String, id: String) = ApiKaboom(
  message = "Foo Item not found.",
  code = ErrorCode.NotFound,
  issues = NonEmptyList.one(ErrorIssue(PathParams, idPathParam, s"couldn't find $id")).some,
  cause = None,
)

def invalidQParams(errs: NonEmptyList[ParseFailure]) = ApiKaboom(
  message = "Invalid query params.",
  code = ErrorCode.Invalid,
  issues = errs.map(w => ErrorIssue(QueryParams, "query", w.sanitized)).some,
  cause = None,
)
