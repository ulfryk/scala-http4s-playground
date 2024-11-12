package foo.domain.model

opaque type FooItemName = String

object FooItemName:
  def apply(txt: String): FooItemName = txt
  def unapply(txt: FooItemName): Option[String] = Some(txt)
