package common.helpers

import cats.data.Validated
import cats.data.Validated.Valid

extension[T] (v: T)
  def some: Option[T] = Some(v)

extension[T, E] (op: Option[Validated[E, T]])
  def swapInnerValidated: Validated[E, Option[T]] =
    op match
      case Some(value) => value.map(_.some)
      case None => Valid(None)
