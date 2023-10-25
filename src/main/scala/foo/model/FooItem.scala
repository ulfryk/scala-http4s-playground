package foo.model

case class FooItem(
  id: FooItemId.Id,
  name: String,
  text: String,
  `type`: FooItemType,
)
