package common.helpers

import cats.data.{NonEmptyChain, Validated, ValidatedNec}

extension[A, B] (either: Either[A, B])
  def toValidatedNec: ValidatedNec[A, B] = Validated.fromEither {
    either.left.map(NonEmptyChain(_))
  }
