package common.helpers

import cats.MonadThrow
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

extension[E, V] (v: Validated[E, V])
  def raiseErrorOnInvalid[F[_] : MonadThrow](fn: E => Throwable): F[V] = v match
    case Invalid(e) => MonadThrow[F].raiseError(fn(e))
    case Valid(v) => MonadThrow[F].pure(v) 
