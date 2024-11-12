package fixtures

import common.api.dto.ApiAnyId
import common.model.AnyId

opaque type TestId = Long
object TestId extends AnyId[TestId](identity, identity)

object ApiTestId extends ApiAnyId("test", TestId)
