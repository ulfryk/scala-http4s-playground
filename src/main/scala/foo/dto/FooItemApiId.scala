package foo.dto

import common.dto.ApiAnyId
import foo.model.FooItemId

object FooItemApiId extends ApiAnyId[FooItemId.type]:
  override protected val impl: FooItemId.type = FooItemId

