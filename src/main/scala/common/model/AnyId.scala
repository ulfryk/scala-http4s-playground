package common.model

open class AnyId[T](up: Long => T, val un: T => Long):
  def apply(raw: Long): T = up(raw)
  def unapply(id: T): Option[Long] = Some(un(id))
