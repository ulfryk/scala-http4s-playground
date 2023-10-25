package common.helpers

extension[T] (v: T)
  def some: Option[T] = Some(v)
