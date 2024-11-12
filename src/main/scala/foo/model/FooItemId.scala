package foo.model

import common.model.AnyId

opaque type FooItemId = Long

object FooItemId extends AnyId[FooItemId](identity, identity)
