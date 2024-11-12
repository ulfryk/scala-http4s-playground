package foo.domain.model

import common.model.AnyId

opaque type FooItemId = Long

object FooItemId extends AnyId[FooItemId](identity, identity)
