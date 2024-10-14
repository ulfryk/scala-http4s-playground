package foo.dto

import foo.model.FooItemType

case class FooItemRequest(
  name: String,
  text: String,
  `type`: FooItemType,
)
