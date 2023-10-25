package common.helpers

import cats.data.{NonEmptyChain, NonEmptyList, Validated, ValidatedNec, ValidatedNel}

extension[A, B] (either: Either[A, B])
  def toValidatedNec: ValidatedNec[A, B] = Validated.fromEither {
    either.left.map(NonEmptyChain(_))
  }

  def toValidatedNel: ValidatedNel[A, B] = Validated.fromEither {
    either.left.map(NonEmptyList(_, Nil))
  }
