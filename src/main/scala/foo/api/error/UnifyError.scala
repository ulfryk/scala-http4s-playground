package foo.api.error

import cats.implicits.*
import common.model.Kaboom
import common.model.Kaboom.InternalKaboom

def unifyError(err: Throwable): Kaboom = err match
  case err: Kaboom => err
  // TODO: org.http4s.MalformedMessageBodyFailure and other subtypes of MessageFailure
  case x => InternalKaboom("Something went wrong.", x.some)
