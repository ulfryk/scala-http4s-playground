package foo.dto

import foo.model.{FooItemId, FooItemType}

case class FooItemResponse(
  id: FooItemId.Id,
  name: String,
  text: String,
  `type`: FooItemType,
)
