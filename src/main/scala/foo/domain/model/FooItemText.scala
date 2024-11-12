package foo.domain.model

opaque type FooItemText = String
 
object FooItemText:
  def apply(txt: String): FooItemText = txt
  def unapply(txt: FooItemText): Option[String] = Some(txt)
